package com.oow.todowithspirit.domain.task;

public enum TaskType {

    SCHEDULE("할일"), ROUTINE("루틴");

    private final String label;

    TaskType(String label) {
        this.label = label;
    }
}
