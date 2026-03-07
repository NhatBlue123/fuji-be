package com.example.fuji.service;

import com.example.fuji.entity.TransactionPayment;
import com.example.fuji.entity.User;
import com.example.fuji.entity.Wallet;
import com.example.fuji.entity.WithdrawRequest;
import com.example.fuji.enums.Role;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.TransactionPaymentRepository;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.repository.WalletRepository;
import com.example.fuji.repository.WithdrawRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawService {

    private final WithdrawRequestRepository withdrawRequestRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionPaymentRepository transactionPaymentRepository;
    private final WalletService walletService;

    public WithdrawRequest requestWithdraw(Long teacherId, Long amount, String bankInfo) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (teacher.getRole() != Role.INSTRUCTOR) {
            throw new BadRequestException("Only instructors can withdraw money");
        }

        if (amount < 50000) {
            throw new BadRequestException("Minimum withdraw amount is 50,000");
        }

        Wallet wallet = walletRepository.findByUserId(teacherId)
                .orElseThrow(() -> new BadRequestException("Wallet not found"));

        walletService.checkAvailableBalance(wallet, amount);

        Long balanceBefore = wallet.getBalance();

        // 1. Freeze money
        wallet.setFrozenBalance(wallet.getFrozenBalance() + amount);
        walletRepository.save(wallet);

        // 2. Log hold
        TransactionPayment tp = new TransactionPayment();
        tp.setUser(teacher);
        tp.setType("WITHDRAW_HOLD");
        tp.setAmount(-amount);
        tp.setBalanceBefore(balanceBefore);
        tp.setBalanceAfter(wallet.getBalance()); // overall balance doesn't change yet, only frozen
        tp.setReferenceId("WD_REQ_" + System.currentTimeMillis());
        tp.setDescription("Đóng băng tiền để chờ duyệt yêu cầu rút tiền về NH: " + bankInfo);
        transactionPaymentRepository.save(tp);

        // 3. Create Request
        WithdrawRequest request = new WithdrawRequest();
        request.setTeacher(teacher);
        request.setAmount(amount);
        request.setBankInfo(bankInfo);
        request.setStatus("PENDING");

        WithdrawRequest saved = withdrawRequestRepository.save(request);
        log.info("Teacher {} requested withdraw of {}", teacherId, amount);
        return saved;
    }

    public void approveWithdraw(Long requestId) {
        WithdrawRequest request = withdrawRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdraw request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("Only PENDING requests can be approved");
        }

        Wallet teacherWallet = walletRepository.findByUserId(request.getTeacher().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Long amount = request.getAmount();

        // 1. Deduct frozen and actual balance
        Long balanceBefore = teacherWallet.getBalance();
        teacherWallet.setFrozenBalance(teacherWallet.getFrozenBalance() - amount);
        teacherWallet.setBalance(balanceBefore - amount);
        walletRepository.save(teacherWallet);

        // 2. Log withdrawal
        TransactionPayment tp = new TransactionPayment();
        tp.setUser(request.getTeacher());
        tp.setType("WITHDRAW");
        tp.setAmount(-amount);
        tp.setBalanceBefore(balanceBefore);
        tp.setBalanceAfter(teacherWallet.getBalance());
        tp.setReferenceId("WD_SUCCESS_" + request.getId());
        tp.setDescription("Admin duyệt rút tiền thành công (NH: " + request.getBankInfo() + ")");
        transactionPaymentRepository.save(tp);

        // 3. Update status
        request.setStatus("SUCCESS");
        withdrawRequestRepository.save(request);

        log.info("Withdraw request {} SUCCESS. Teacher {} withdrew {}", request.getId(), request.getTeacher().getId(),
                amount);
    }
}
