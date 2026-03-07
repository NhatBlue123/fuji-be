package com.example.fuji.repository;
import com.example.fuji.entity.TransactionPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionPaymentRepository extends JpaRepository<TransactionPayment, Long> {
    Page<TransactionPayment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
