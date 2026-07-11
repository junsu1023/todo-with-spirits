
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
    }

    object LOGIN {
        private const val AUTH = "$API/auth"
        const val LOGIN = "$AUTH/login"
    }
}