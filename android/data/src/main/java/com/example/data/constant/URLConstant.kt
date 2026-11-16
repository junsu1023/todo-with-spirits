
package com.example.data.constant

object URLConstant {
    const val API = "api"

    object HEALTH {
        private const val ACTUATOR = "actuator"
        const val HEALTH = "$ACTUATOR/health"
    }

    object TASK {
        private const val TASK = "$API/task"
        // 일정 목록 조회(GET, from/to 필수)·생성(POST)·삭제(DELETE)가 같은 경로를 쓴다.
        const val TASK_LIST = TASK
        const val TASK_DETAIL = "$TASK/{taskId}"
        const val TASK_COMPLETE = "$TASK/{taskId}/complete"
        const val TASK_DELETE = TASK
        const val TASK_POSTPONE = "$TASK/{taskId}/postpone"
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
        const val USER_SUBSCRIPTION = "$USER_ME/subscription"
        const val EMAIL_VERIFY_RESEND = "$USER_ME/email/verify/send" // 로그인 상태에서 재발송
        const val EMAIL_VERIFY_SEND = "$USER/email/verify/send" // 비로그인 상태(회원가입 전)에서 발송
        const val EMAIL_VERIFY = "$USER/email/verify" // 이메일 인증코드 검증 (미인증유저 -> 인증유저)
    }

    object NOTIFICATION {
        const val NOTIFICATION = "$API/notification"
        const val NOTIFICATION_READ = "$NOTIFICATION/{alarmId}/read"
        const val NOTIFICATION_READ_ALL = "$NOTIFICATION/read"
    }

    object SETTING {
        private const val SETTING = "$API/setting"
        const val SETTING_DISPLAY = "$SETTING/display"
        const val SETTING_NOTIFICATION = "$SETTING/notification"
    }

    object SPIRIT {
        const val SPIRIT = "$API/spirit"
        const val SPIRIT_CURRENT = "$SPIRIT/current"
        const val SPIRIT_NAME = "$SPIRIT/{spiritId}/name"
        const val SPIRIT_MEET = "$SPIRIT/meet"
    }

    object DECORATION {
        const val DECORATION = "$API/decorations"
        const val DECORATION_PURCHASE = "$DECORATION/{decorationId}/purchase"
        const val DECORATION_INSTALL = "$DECORATION/{decorationId}/install"
    }

    object CURRENCY {
        const val CURRENCY = "$API/currency"
    }
}