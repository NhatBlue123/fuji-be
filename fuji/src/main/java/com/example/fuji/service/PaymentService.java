package com.example.fuji.service;

import com.example.fuji.dto.request.XGateCallbackRequest;
import com.example.fuji.dto.response.PaymentResponse;
import com.example.fuji.entity.Payment;
import com.example.fuji.entity.TransactionPayment;
import com.example.fuji.entity.User;
import com.example.fuji.entity.Wallet;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.PaymentRepository;
import com.example.fuji.repository.TransactionPaymentRepository;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionPaymentRepository transactionPaymentRepository;

    @Value("${xgate.secret-key}")
    private String secretKey;

    // -------------------------------------------------------------------------
    // Tạo payment (PENDING)
    // -------------------------------------------------------------------------
    public PaymentResponse createPayment(Long userId, Long amount) {

        if (amount == null || amount <= 0) {
            throw new BadRequestException("Số tiền nạp phải lớn hơn 0");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setOrderId("ORDER_" + UUID.randomUUID());
        payment.setStatus("PENDING");

        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Xử lý callback từ XGate
    // -------------------------------------------------------------------------
    public void handleCallback(XGateCallbackRequest request) {

        Payment payment = paymentRepository.findByOrderId(request.getOrder_id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + request.getOrder_id()));

        // Idempotency – tránh cộng tiền 2 lần
        if (!"PENDING".equals(payment.getStatus())) {
            log.warn("Callback received for already-processed order: {}", request.getOrder_id());
            return;
        }

        // Verify HMAC signature
        if (!verifySignature(request)) {
            throw new BadRequestException("Invalid signature for order: " + request.getOrder_id());
        }

        if ("SUCCESS".equals(request.getStatus())) {

            payment.setStatus("SUCCESS");
            payment.setGatewayTransactionId(request.getTransaction_id());

            // Cộng tiền vào wallet
            User user = payment.getUser();

            Wallet wallet = walletRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        Wallet newWallet = new Wallet();
                        newWallet.setUser(user);
                        newWallet.setBalance(0L);
                        newWallet.setFrozenBalance(0L);
                        return walletRepository.save(newWallet);
                    });

            Long balanceBefore = wallet.getBalance();
            Long balanceAfter = balanceBefore + payment.getAmount();

            wallet.setBalance(balanceAfter);
            walletRepository.save(wallet);

            // Tạo transaction history
            TransactionPayment tp = new TransactionPayment();
            tp.setUser(user);
            tp.setType("TOPUP");
            tp.setAmount(payment.getAmount());
            tp.setBalanceBefore(balanceBefore);
            tp.setBalanceAfter(balanceAfter);
            tp.setReferenceId(payment.getOrderId());
            tp.setDescription("Topup via XGate | txn=" + request.getTransaction_id());
            transactionPaymentRepository.save(tp);

            log.info("Wallet topped up: userId={} amount={} balanceAfter={}",
                    user.getId(), payment.getAmount(), balanceAfter);

        } else {
            payment.setStatus("FAILED");
        }

        paymentRepository.save(payment);
    }

    // -------------------------------------------------------------------------
    // Verify HMAC-SHA256 signature từ XGate
    // -------------------------------------------------------------------------
    public boolean verifySignature(XGateCallbackRequest request) {
        try {
            // Signature = HMAC-SHA256(order_id + amount + status, secret_key) → Base64
            String data = request.getOrder_id()
                    + String.valueOf(request.getAmount())
                    + request.getStatus();

            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes("UTF-8"), "HmacSHA256");
            sha256.init(keySpec);

            byte[] hash = sha256.doFinal(data.getBytes("UTF-8"));
            String computedSignature = Base64.getEncoder().encodeToString(hash);

            // DEBUG – xoá khi lên production
            log.info("[SIG-DEBUG] data     = '{}'", data);
            log.info("[SIG-DEBUG] computed = '{}'", computedSignature);
            log.info("[SIG-DEBUG] received = '{}'", request.getSignature());

            return computedSignature.equals(request.getSignature());

        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * [DEV ONLY] Tính signature để dùng trong Postman test – XOÁ trước production.
     */
    public String computeSignatureForTest(String orderId, Long amount, String status) {
        try {
            String data = orderId + String.valueOf(amount) + status;
            Mac sha256 = Mac.getInstance("HmacSHA256");
            sha256.init(new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(sha256.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException("Cannot compute signature", e);
        }
    }

    // -------------------------------------------------------------------------
    // Map entity → DTO (không expose User entity)
    // -------------------------------------------------------------------------
    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .gateway(payment.getGateway())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .userId(payment.getUser().getId())
                .userEmail(payment.getUser().getEmail())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
