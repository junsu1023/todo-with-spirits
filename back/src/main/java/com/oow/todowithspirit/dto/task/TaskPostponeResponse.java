package com.oow.todowithspirit.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class TaskPostponeResponse {

    private Long taskId;
    private LocalDate originalDate;
    private LocalDate postponedDate;
    private LocalTime postponedTime;
    private int postponeCount;
    private Integer maxPostponeCount;      // null이면 무제한
    private Integer remainingPostponeCount; // null이면 무제한
}