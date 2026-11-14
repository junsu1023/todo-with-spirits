package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.task.*;
import com.oow.todowithspirit.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ==============================================
    // 생성 / 수정 (단발성 · 반복 공통, repeatType으로 구분)
    // ==============================================

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskService.createTask(userId, request)));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateTask(userId, taskId, request)));
    }

    // ==============================================
    // 삭제 (단건 · 다건 공통)
    // ==============================================

    @DeleteMapping
    public ResponseEntity<ApiResponse<TaskDeleteResponse>> deleteTasks(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TaskDeleteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.deleteTasks(userId, request)));
    }

    // ==============================================
    // 조회
    // ==============================================

    // 일정 목록 (occurrence 목록 + 완료 집계)
    @GetMapping()
    public ResponseEntity<ApiResponse<CalendarTasksResponse>> getCalendarTasks(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getCalendarTasks(userId, from, to)));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskOccurrenceResponse>> getTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskDetail(userId, taskId, targetDate)));
    }

    // ==============================================
    // 미루기
    // ==============================================

    @PatchMapping("/{taskId}/postpone")
    public ResponseEntity<ApiResponse<TaskPostponeResponse>> postponeTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskPostponeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.postponeTask(userId, taskId, request)));
    }

    // ==============================================
    // 완료 / 완료 취소
    // ==============================================

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<TaskCompleteResponse>> completeTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(taskService.completeTask(userId, taskId, date)));
    }

    @DeleteMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<Void>> undoCompleteTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        taskService.undoCompleteTask(userId, taskId, date);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
