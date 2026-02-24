package com.example.fuji.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.Otp;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    java.util.Optional<Otp> findByEmailAndOtpCode(String email, String otpCode);

    java.util.Optional<Otp> findByEmail(String email);

    void deleteByEmail(String email);

}