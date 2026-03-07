package com.example.fuji.repository;

import com.example.fuji.entity.WithdrawRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WithdrawRequestRepository extends JpaRepository<WithdrawRequest, Long> {
    List<WithdrawRequest> findByTeacherId(Long teacherId);

    List<WithdrawRequest> findByStatus(String status);
}
