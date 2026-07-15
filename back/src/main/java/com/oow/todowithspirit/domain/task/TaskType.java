package com.oow.todowithspirit.domain.task;

public enum TaskType {

    SCHEDULE("일정"), ROUTINE("루틴");

    private final String label;

    TaskType(String label) {
        this.label = label;
    }
}
