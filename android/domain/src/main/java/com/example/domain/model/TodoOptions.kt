package com.example.domain.model

enum class RepeatOption(val displayName: String) {
    NONE("없음"),
    DAILY("매일"),
    WEEKLY("매주"),
    MONTHLY("매월"),
    YEARLY("매년");

    companion object {
        fun fromDisplayName(name: String): RepeatOption {
            return entries.find { it.displayName == name } ?: NONE
        }
        fun getAllDisplayNames() = entries.map { it.displayName }
    }
}

enum class AlarmOption(val displayName: String) {
    NONE("없음"),
    TEN_MINUTES("10분 전"),
    THIRTY_MINUTES("30분 전"),
    ONE_HOUR("1시간 전");

    companion object {
        fun fromDisplayName(name: String): AlarmOption {
            return entries.find { it.displayName == name } ?: NONE
        }
        fun getAllDisplayNames() = entries.map { it.displayName }
    }
}

enum class PlanSortOption(val displayName: String) {
    DEADLINE("마감 임박 순"),
    IMPORTANT("중요 일정 순"),
    COMPLETE("완료 순");

    companion object {
        fun fromDisplayName(name: String): PlanSortOption {
            return entries.find { it.displayName == name } ?: DEADLINE
        }
        fun getAllDisplayNames() = entries.map { it.displayName }
    }
}