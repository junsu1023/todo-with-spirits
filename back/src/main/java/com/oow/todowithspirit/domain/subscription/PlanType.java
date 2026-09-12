package com.oow.todowithspirit.domain.subscription;

public enum PlanType {
    FREE("미구독"),
    MONTHLY("월간구독"),
    YEARLY("연간구독");

    private final String description;

    PlanType(String description) {
        this.description = description;
    }
}