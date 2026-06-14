package com.oow.todowithspirit.domain.task;

public enum TaskType {

    TODO("할일"), SCHEDULE("일정"), HABIT("습관");

    private final String label;

    TaskType(String label) {
        this.label = label;
    }
}
