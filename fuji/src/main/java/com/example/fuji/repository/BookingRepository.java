package com.example.fuji.repository;

import com.example.fuji.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStudentId(Long studentId);

    List<Booking> findByTeacherId(Long teacherId);
}
