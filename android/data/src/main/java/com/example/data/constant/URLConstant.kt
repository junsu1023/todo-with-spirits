
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
        const val CHECK_EMAIL = "$AUTH/check-email"
    }

    object USER {
        private const val USER = "$API/user"
        const val USER_ME = "$USER/me"
        const val EMAIL_VERIFY_RESEND = "$USER_ME/email/verify/resend" // 로그인 상태에서 재발송
        const val EMAIL_VERIFY_SEND = "$USER/email/verify/send" // 비로그인 상태(회원가입 전)에서 발송
        const val EMAIL_VERIFY = "$USER/email/verify" // 이메일 인증코드 검증 (미인증유저 -> 인증유저)
    }

    object RECORD {
        private const val RECORD = "$API/record"
        const val RECORD_TODAY = "$RECORD/today"
        const val RECORD_WEEKLY = "$RECORD/weekly"
        const val RECORD_MONTHLY = "$RECORD/monthly"
    }

    object NOTIFICATION {
        const val NOTIFICATION = "$API/notification"
        const val NOTIFICATION_READ = "$NOTIFICATION/{alarmId}/read"
        const val NOTIFICATION_READ_ALL = "$NOTIFICATION/read"
    }

    object SETTING {
        private const val SETTING = "$API/setting"
        const val SETTING_DISPLAY = "$SETTING/display"
    }
}