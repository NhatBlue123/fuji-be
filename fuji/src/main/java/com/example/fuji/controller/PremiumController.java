package com.example.fuji.controller;

import com.example.fuji.security.UserPrincipal;
import com.example.fuji.service.PremiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/premium")
@RequiredArgsConstructor
public class PremiumController {

    private final PremiumService premiumService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(
            @AuthenticationPrincipal UserPrincipal principal) {
        premiumService.subscribePremium(principal.getId());
        return ResponseEntity.ok("Successfully subscribed to Premium for 30 days");
    }
}
