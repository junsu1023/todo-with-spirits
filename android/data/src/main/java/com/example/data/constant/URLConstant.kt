
package com.example.data.constant

object URLConstant {
    const val BASE_URL = "https://todo-with-spirits.onrender.com"

    object HEALTH {
        private const val ACTUATOR = "actuator"
        const val HEALTH = "$ACTUATOR/health"
    }

    object TASK {
        private const val TASK = "api/task"
        const val TASK_DETAIL = "$TASK/{taskId}"
    }
}