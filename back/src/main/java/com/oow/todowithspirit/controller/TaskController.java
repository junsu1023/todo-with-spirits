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
    // 삭제 (단건 · 다건 공통)
    // ==============================================

    @DeleteMapping
    public ResponseEntity<ApiResponse<TaskDeleteResponse>> deleteTasks(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TaskDeleteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.deleteTasks(userId, request)));
    }

    // ==============================================
    // 단건 조회 (일정/루틴 공통)
    // ==============================================

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<RoutineCreateResponse>> getTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskDetail(userId, taskId)));
    }

    // ==============================================
    // 일정 수정
    // ==============================================

    @PatchMapping("/schedule/{taskId}")
    public ResponseEntity<ApiResponse<RoutineCreateResponse>> updateSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateSchedule(userId, taskId, request)));
    }

    @PatchMapping("/routine/{taskId}")
    public ResponseEntity<ApiResponse<RoutineCreateResponse>> updateRoutine(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId,
            @Valid @RequestBody RoutineUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateRoutine(userId, taskId, request)));
    }

    // ==============================================
    // 완료 / 완료 취소
    // ==============================================

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        taskService.completeTask(userId, taskId, date);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<Void>> undoCompleteTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        taskService.undoCompleteTask(userId, taskId, date);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ==============================================
    // 캘린더 (일정 + 루틴 occurrence 통합 조회)
    // ==============================================

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<CalendarTaskListResponse>> getCalendarTasks(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getCalendarTasks(userId, from, to)));
    }

    // ==============================================
    // 일정 (SCHEDULE)
    // ==============================================

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<ScheduleCreateResponse>> createSchedule(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ScheduleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskService.createSchedule(userId, request)));
    }

    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<TaskListResponse<TaskSummaryResponse>>> getSchedules(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getSchedules(userId, from, to)));
    }

    // ==============================================
    // 루틴 (ROUTINE)
    // ==============================================

    @PostMapping("/routine")
    public ResponseEntity<ApiResponse<RoutineCreateResponse>> createRoutine(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RoutineCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskService.createRoutine(userId, request)));
    }

    @GetMapping("/routine")
    public ResponseEntity<ApiResponse<TaskListResponse<TaskSummaryResponse>>> getRoutines(
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        return ResponseEntity.ok(ApiResponse.success(taskService.getRoutines(userId, targetDate)));
    }
}
