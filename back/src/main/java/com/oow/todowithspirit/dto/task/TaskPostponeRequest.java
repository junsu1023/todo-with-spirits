package com.oow.todowithspirit.dto.task;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class TaskPostponeRequest {

    // 반복 할 일만 필수 — 미룰 대상 occurrence의 원래 발생일. 단발성은 무시됨(내부에서 자동 판단).
    private LocalDate originalDate;

    // null이면 날짜는 유지하고 시간만 변경
    private LocalDate newDate;

    // null이면 시간은 기존 값 유지
    private LocalTime newTime;
}