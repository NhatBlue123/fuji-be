package com.example.fuji.service;

import com.example.fuji.entity.Course;
import com.example.fuji.entity.TransactionPayment;
import com.example.fuji.entity.User;
import com.example.fuji.entity.UserCourseProgress;
import com.example.fuji.entity.Wallet;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.CourseRepository;
import com.example.fuji.repository.TransactionPaymentRepository;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.repository.UserCourseProgressRepository;
import com.example.fuji.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoursePaymentService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final TransactionPaymentRepository transactionPaymentRepository;
    private final WalletService walletService;

    public void purchaseCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        // Check already bought
        if (userCourseProgressRepository.findByUserIdAndCourseId(userId, courseId).isPresent()) {
            throw new BadRequestException("You have already purchased this course");
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Wallet not found. Please top up first."));

        Long price = course.getPrice() != null ? course.getPrice().longValue() : 0L;

        // check if free
        if (price > 0) {
            walletService.checkAvailableBalance(wallet, price);

            Long balanceBefore = wallet.getBalance();
            Long balanceAfter = balanceBefore - price;

            // deduct balance
            wallet.setBalance(balanceAfter);
            walletRepository.save(wallet);

            // create transaction
            TransactionPayment tp = new TransactionPayment();
            tp.setUser(user);
            tp.setType("COURSE_PAYMENT");
            tp.setAmount(-price);
            tp.setBalanceBefore(balanceBefore);
            tp.setBalanceAfter(balanceAfter);
            tp.setReferenceId("COURSE_" + courseId);
            tp.setDescription("Mua khóa học: " + course.getTitle());
            transactionPaymentRepository.save(tp);
        }

        // Create enrollment
        UserCourseProgress progress = new UserCourseProgress();
        progress.setUser(user);
        progress.setCourse(course);
        userCourseProgressRepository.save(progress);

        // optional: inc course student count
        course.setStudentCount(course.getStudentCount() + 1);
        courseRepository.save(course);

        log.info("User {} successfully purchased course {}", userId, courseId);
    }
}
