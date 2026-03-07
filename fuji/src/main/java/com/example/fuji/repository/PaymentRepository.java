package com.example.fuji.repository;
import com.example.fuji.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>{
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);
}
