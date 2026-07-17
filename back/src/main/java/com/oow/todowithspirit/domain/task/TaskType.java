package com.oow.todowithspirit.domain.task;

public enum TaskType {

    TODO("할일"), HABIT("루틴");

    private final String label;

    TaskType(String label) {
        this.label = label;
    }
}
