package com.oow.todowithspirit.domain.task;

public final class TaskPostponePolicy {

    private static final int DAILY_MAX = 2;
    private static final int WEEKLY_MAX = 2;
    private static final int MONTHLY_MAX = 3;

    private TaskPostponePolicy() {
    }

    /**
     * null이면 무제한 (단발성 할 일)
     */
    public static Integer maxPostponeCount(Task task) {
        if (!task.isRepeating()) {
            return null;
        }
        return switch (task.getRepeatType()) {
            case DAILY -> DAILY_MAX;
            case WEEKLY -> WEEKLY_MAX;
            case MONTHLY -> MONTHLY_MAX;
            default -> null;
        };
    }
}