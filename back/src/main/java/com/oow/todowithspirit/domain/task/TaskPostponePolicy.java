package com.oow.todowithspirit.domain.task;

public final class TaskPostponePolicy {

    private static final int DAILY_MAX = 2;
    private static final int WEEKLY_MAX = 2;
    private static final int MONTHLY_MAX = 3;

    private TaskPostponePolicy() {
    }

    /**
     * null이면 무제한 (SCHEDULE)
     */
    public static Integer maxPostponeCount(Task task) {
        if (task.getTaskType() != TaskType.ROUTINE) {
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