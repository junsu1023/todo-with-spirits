
package com.example.data.constant

object URLConstant {
    const val API = "api"

    object HEALTH {
        private const val ACTUATOR = "actuator"
        const val HEALTH = "$ACTUATOR/health"
    }

    object TASK {
        private const val TASK = "$API/task"
        const val TASK_DETAIL = "$TASK/{taskId}"
        const val TASK_CALENDAR = "$TASK/calendar"
        const val TASK_ROUTINE = "$TASK/routine"
        const val TASK_SCHEDULE = "$TASK/schedule"
        const val TASK_COMPLETE = "$TASK/{taskId}/complete"
        const val TASK_DELETE = TASK
        const val TASK_SCHEDULE_DETAIL = "$TASK/schedule/{taskId}"
        const val TASK_ROUTINE_DETAIL = "$TASK/routine/{taskId}"
    }

    object LOGIN {
        private const val AUTH = "$API/auth"
        const val LOGIN = "$AUTH/login"
        const val LOGOUT = "$AUTH/logout"
        const val SIGNUP = "$AUTH/signup"
        const val REISSUE = "$AUTH/reissue"
        const val SOCIAL_LOGIN = "$AUTH/social/login"
    }

    object RECORD {
        private const val RECORD = "$API/record"
        const val RECORD_TODAY = "$RECORD/today"
        const val RECORD_WEEKLY = "$RECORD/weekly"
    }
}