package com.example.fuji.scheduler;

import com.example.fuji.entity.JlptTestAttempt;
import com.example.fuji.entity.enums.AttemptStatus;
import com.example.fuji.repository.JlptTestAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamStatusScheduler {

    private final JlptTestAttemptRepository attemptRepository;

    /**
     * Chạy định kỳ mỗi 5 phút để tìm những attempt đã hết giờ và cập nhật trạng
     * thái
     */
    @Scheduled(fixedRate = 300000) // 5 phút
    @Transactional
    public void expireOldAttempts() {
        log.info("Running Exam Status Scheduler: Checking for expired IN_PROGRESS attempts...");

        LocalDateTime now = LocalDateTime.now();
        List<JlptTestAttempt> expiredAttempts = attemptRepository
                .findExpiredInProgressAttempts(AttemptStatus.IN_PROGRESS, now);

        if (!expiredAttempts.isEmpty()) {
            expiredAttempts.forEach(attempt -> {
                attempt.setStatus(AttemptStatus.EXPIRED);
                // Bạn cũng có thể thiết lập điểm 0 ở đây nếu cần thiết,
                // nhưng EXPIRED đã đủ đánh dấu bài thi không được công nhận.
            });

            attemptRepository.saveAll(expiredAttempts);
            log.info("Expired {} attempts that ran out of time.", expiredAttempts.size());
        }
    }
}
