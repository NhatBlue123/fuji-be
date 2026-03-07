package com.example.fuji.controller;

import com.example.fuji.security.UserPrincipal;
import com.example.fuji.service.CoursePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CoursePaymentController {

    private final CoursePaymentService coursePaymentService;

    @PostMapping("/{courseId}/buy")
    public ResponseEntity<String> buyCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long courseId) {

        coursePaymentService.purchaseCourse(principal.getId(), courseId);
        return ResponseEntity.ok("Course purchased successfully");
    }
}
