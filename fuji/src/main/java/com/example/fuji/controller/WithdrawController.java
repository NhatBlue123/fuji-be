package com.example.fuji.controller;

import com.example.fuji.dto.request.WithdrawDto;
import com.example.fuji.entity.WithdrawRequest;
import com.example.fuji.security.UserPrincipal;
import com.example.fuji.service.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/withdraw")
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawService withdrawService;

    @PostMapping
    public ResponseEntity<WithdrawRequest> requestWithdraw(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody WithdrawDto dto) {

        WithdrawRequest request = withdrawService.requestWithdraw(
                principal.getId(),
                dto.getAmount(),
                dto.getBankInfo());

        return ResponseEntity.ok(request);
    }
}
