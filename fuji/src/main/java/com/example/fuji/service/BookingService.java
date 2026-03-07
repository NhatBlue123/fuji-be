package com.example.fuji.service;

import com.example.fuji.entity.Booking;
import com.example.fuji.entity.TransactionPayment;
import com.example.fuji.entity.User;
import com.example.fuji.entity.Wallet;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.BookingRepository;
import com.example.fuji.repository.TransactionPaymentRepository;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionPaymentRepository transactionPaymentRepository;
    private final WalletService walletService;

    // Admin platform fee configuration (e.g. 10%)
    private static final double PLATFORM_FEE_PERCENTAGE = 0.10;
    private static final Long ADMIN_ID = 1L; // Assuming ID 1 is the main admin wallet

    public Booking createBooking(Long studentId, Long teacherId, Long price, LocalDateTime scheduledAt) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        Wallet studentWallet = walletRepository.findByUserId(studentId)
                .orElseThrow(() -> new BadRequestException("Wallet not found. Please top up first."));

        // check available balance
        walletService.checkAvailableBalance(studentWallet, price);

        Long balanceBefore = studentWallet.getBalance();

        // hold the balance
        studentWallet.setFrozenBalance(studentWallet.getFrozenBalance() + price);
        walletRepository.save(studentWallet);

        // create booking
        Booking booking = new Booking();
        booking.setStudent(student);
        booking.setTeacher(teacher);
        booking.setPrice(price);
        booking.setStatus("PENDING");
        booking.setScheduledAt(scheduledAt);
        Booking savedBooking = bookingRepository.save(booking);

        // transaction log
        TransactionPayment tp = new TransactionPayment();
        tp.setUser(student);
        tp.setType("BOOKING_HOLD");
        tp.setAmount(-price);
        tp.setBalanceBefore(balanceBefore);
        tp.setBalanceAfter(balanceBefore); // total balance doesn't change yet, just frozen
        tp.setReferenceId("BOOKING_" + savedBooking.getId());
        tp.setDescription("Đóng băng tiền để đặt lịch với GV: " + teacher.getUsername());
        transactionPaymentRepository.save(tp);

        log.info("Student {} booked teacher {}, price held: {}", studentId, teacherId, price);
        return savedBooking;
    }

    public void completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new BadRequestException("Booking is already " + booking.getStatus());
        }

        Long price = booking.getPrice();
        Long platformFee = (long) (price * PLATFORM_FEE_PERCENTAGE);
        Long teacherAmount = price - platformFee;

        // 1. Deduct frozen and actual balance from student
        Wallet studentWallet = walletRepository.findByUserId(booking.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student wallet not found"));

        Long studentBalanceBefore = studentWallet.getBalance();
        studentWallet.setFrozenBalance(studentWallet.getFrozenBalance() - price);
        studentWallet.setBalance(studentWallet.getBalance() - price);
        walletRepository.save(studentWallet);

        // Log student payment
        TransactionPayment tpStudent = new TransactionPayment();
        tpStudent.setUser(booking.getStudent());
        tpStudent.setType("BOOKING_PAYMENT");
        tpStudent.setAmount(-price);
        tpStudent.setBalanceBefore(studentBalanceBefore);
        tpStudent.setBalanceAfter(studentWallet.getBalance());
        tpStudent.setReferenceId("BOOKING_" + bookingId);
        tpStudent.setDescription("Thanh toán thành công lịch học với GV: " + booking.getTeacher().getUsername());
        transactionPaymentRepository.save(tpStudent);

        // 2. Add to teacher
        Wallet teacherWallet = walletRepository.findByUserId(booking.getTeacher().getId())
                .orElseGet(() -> createEmptyWallet(booking.getTeacher()));

        Long teacherBalanceBefore = teacherWallet.getBalance();
        teacherWallet.setBalance(teacherWallet.getBalance() + teacherAmount);
        walletRepository.save(teacherWallet);

        TransactionPayment tpTeacher = new TransactionPayment();
        tpTeacher.setUser(booking.getTeacher());
        tpTeacher.setType("BOOKING_PAYMENT");
        tpTeacher.setAmount(teacherAmount);
        tpTeacher.setBalanceBefore(teacherBalanceBefore);
        tpTeacher.setBalanceAfter(teacherWallet.getBalance());
        tpTeacher.setReferenceId("BOOKING_" + bookingId);
        tpTeacher.setDescription("Doanh thu dạy học từ HV: " + booking.getStudent().getUsername());
        transactionPaymentRepository.save(tpTeacher);

        // 3. Add to admin (platform fee)
        User admin = userRepository.findById(ADMIN_ID).orElse(null);
        if (admin != null) {
            Wallet adminWallet = walletRepository.findByUserId(ADMIN_ID)
                    .orElseGet(() -> createEmptyWallet(admin));

            Long adminBalanceBefore = adminWallet.getBalance();
            adminWallet.setBalance(adminWallet.getBalance() + platformFee);
            walletRepository.save(adminWallet);

            TransactionPayment tpAdmin = new TransactionPayment();
            tpAdmin.setUser(admin);
            tpAdmin.setType("PLATFORM_FEE");
            tpAdmin.setAmount(platformFee);
            tpAdmin.setBalanceBefore(adminBalanceBefore);
            tpAdmin.setBalanceAfter(adminWallet.getBalance());
            tpAdmin.setReferenceId("BOOKING_" + bookingId);
            tpAdmin.setDescription("Phí nền tảng từ booking: " + bookingId);
            transactionPaymentRepository.save(tpAdmin);
        }

        // Update booking status
        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);

        log.info("Booking {} completed. Student paid {}, Teacher received {}, Platform fee {}",
                bookingId, price, teacherAmount, platformFee);
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new BadRequestException("Only PENDING bookings can be cancelled");
        }

        Long price = booking.getPrice();

        // Release frozen balance
        Wallet studentWallet = walletRepository.findByUserId(booking.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student wallet not found"));

        studentWallet.setFrozenBalance(studentWallet.getFrozenBalance() - price);
        walletRepository.save(studentWallet);

        // Log refund
        TransactionPayment tp = new TransactionPayment();
        tp.setUser(booking.getStudent());
        tp.setType("REFUND");
        tp.setAmount(price);
        tp.setBalanceBefore(studentWallet.getBalance());
        tp.setBalanceAfter(studentWallet.getBalance()); // balance didn't change, just unfrozen
        tp.setReferenceId("BOOKING_" + bookingId);
        tp.setDescription("Hoàn tiền do hủy lịch học với GV: " + booking.getTeacher().getUsername());
        transactionPaymentRepository.save(tp);

        // Update status
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        log.info("Booking {} cancelled. Refunded {} to frozen balance", bookingId, price);
    }

    private Wallet createEmptyWallet(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(0L);
        wallet.setFrozenBalance(0L);
        return walletRepository.save(wallet);
    }
}
