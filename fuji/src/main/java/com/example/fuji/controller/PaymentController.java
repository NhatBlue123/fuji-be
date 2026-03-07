package com.example.fuji.controller;

import com.example.fuji.dto.request.PaymentRequest;
import com.example.fuji.dto.request.XGateCallbackRequest;
import com.example.fuji.dto.response.PaymentResponse;
import com.example.fuji.security.UserPrincipal;
import com.example.fuji.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Tạo payment để nạp tiền.
     * userId lấy từ JWT (không nhận từ request body để tránh giả mạo).
     */
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(
                principal.getId(), request.getAmount());
        return ResponseEntity.ok(response);
    }

    /**
     * [DEV ONLY] Trả về signature đúng để dùng trong Postman.
     * XOÁ endpoint này trước khi deploy production.
     *
     * Ví dụ: GET /api/payments/test-signature
     * ?order_id=ORDER_xxx&amount=10000&status=SUCCESS
     */
    @GetMapping("/test-signature")
    public ResponseEntity<String> testSignature(
            @RequestParam("order_id") String orderId,
            @RequestParam("amount") Long amount,
            @RequestParam("status") String status) {

        String sig = paymentService.computeSignatureForTest(orderId, amount, status);
        return ResponseEntity.ok(sig);
    }

    /**
     * Callback từ XGate – public endpoint, không cần JWT.
     * Lỗi được handle bởi GlobalExceptionHandler:
     * - Order không tồn tại → 404
     * - Signature sai → 400
     */
    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(
            @RequestBody XGateCallbackRequest request) {

        paymentService.handleCallback(request);
        return ResponseEntity.ok("OK");
    }
}