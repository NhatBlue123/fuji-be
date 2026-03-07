package com.example.fuji.controller;

import com.example.fuji.dto.request.BookingRequest;
import com.example.fuji.entity.Booking;
import com.example.fuji.security.UserPrincipal;
import com.example.fuji.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(
                principal.getId(),
                request.getTeacherId(),
                request.getPrice(),
                request.getScheduledAt());
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<String> completeBooking(@PathVariable Long id) {
        // In a real scenario, this might be triggered by teacher/admin/student when
        // class ends
        bookingService.completeBooking(id);
        return ResponseEntity.ok("Booking completed successfully");
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        // Only PENDING booking can be cancelled
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled successfully");
    }
}
