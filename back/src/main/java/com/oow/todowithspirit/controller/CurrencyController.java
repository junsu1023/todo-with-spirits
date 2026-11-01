package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.currency.CurrencyResponse;
import com.oow.todowithspirit.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<ApiResponse<CurrencyResponse>> getCurrency(
            @AuthenticationPrincipal Long userId) {
        CurrencyResponse response = CurrencyResponse.of(
                currencyService.getBalance(userId),
                currencyService.getRecentTransactions(userId)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
