package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.record.DailyRecordResponse;
import com.oow.todowithspirit.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/record")
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<DailyRecordResponse>> getTodayRecord(
            @AuthenticationPrincipal Long userId) {

        DailyRecordResponse response = recordService.getDailyRecord(userId, LocalDate.now());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}