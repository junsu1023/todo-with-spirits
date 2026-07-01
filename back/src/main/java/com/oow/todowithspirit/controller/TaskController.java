package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.task.RoutineCreateRequest;
import com.oow.todowithspirit.dto.task.ScheduleCreateRequest;
import com.oow.todowithspirit.dto.task.TaskCreateResponse;
import com.oow.todowithspirit.dto.task.TaskListResponse;
import com.oow.todowithspirit.dto.task.TaskSummaryResponse;
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
    // SCHEDULE
    // ==============================================

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> createSchedule(
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

    @GetMapping("/schedule/{taskId}")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> getSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getScheduleDetail(userId, taskId)));
    }

    // ==============================================
    // ROUTINE
    // ==============================================

    @PostMapping("/routine")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> createRoutine(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RoutineCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskService.createRoutine(userId, request)));
    }

    @GetMapping("/routine")
    public ResponseEntity<ApiResponse<TaskListResponse<TaskSummaryResponse>>> getRoutines(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getRoutines(userId)));
    }

    @GetMapping("/routine/{taskId}")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> getRoutine(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getRoutineDetail(userId, taskId)));
    }
}
