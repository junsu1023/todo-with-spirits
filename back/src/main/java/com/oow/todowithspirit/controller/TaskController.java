package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.task.RoutineCreateRequest;
import com.oow.todowithspirit.dto.task.ScheduleCreateRequest;
import com.oow.todowithspirit.dto.task.TaskCreateResponse;
import com.oow.todowithspirit.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> createSchedule(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ScheduleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskService.createSchedule(userId, request)));
    }

    @PostMapping("/routine")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> createRoutine(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RoutineCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskService.createRoutine(userId, request)));
    }
}
