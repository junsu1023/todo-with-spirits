
package com.example.data.constant

object URLConstant {
    const val BASE_URL = "https://todo-with-spirits.onrender.com"
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
    }

    object LOGIN {
        private const val AUTH = "$API/auth"
        const val LOGIN = "$AUTH/login"
    }
}