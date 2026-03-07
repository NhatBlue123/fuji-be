package com.example.fuji.controller;

import com.example.fuji.service.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/withdraw")
@RequiredArgsConstructor
public class AdminWithdrawController {

    private final WithdrawService withdrawService;

    // TODO: Depending on your security setup, you might need
    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approveWithdraw(@PathVariable Long id) {
        withdrawService.approveWithdraw(id);
        return ResponseEntity.ok("Withdrawal approved successfully");
    }
}
