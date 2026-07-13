package com.example.domain.model

enum class RepeatOption(val displayName: String) {
    NONE("안 함"),
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
    NONE("안 함"),
    TEN_MIN_BEFORE("10분 전"),
    THIRTY_MIN_BEFORE("30분 전"),
    ONE_HOUR_BEFORE("1시간 전");

    companion object {
        fun fromDisplayName(name: String): AlarmOption {
            return entries.find { it.displayName == name } ?: NONE
        }
        fun getAllDisplayNames() = entries.map { it.displayName }
    }
}

enum class CategoryOption(val displayName: String) {
    NONE("미정"),
    RELATIONSHIP("인간관계/약속"),
    SELF_DEVELOPMENT("자기계발"),
    WORK("업무"),
    HOBBY("취미");

    companion object {
        fun fromDisplayName(name: String): CategoryOption {
            return entries.find { it.displayName == name } ?: NONE
        }
        fun getAllDisplayNames() = entries.map { it.displayName }
    }
}

enum class PublicStateOption(val displayName: String) {
    PUBLIC("공개"),
    PRIVATE("비공개");

    companion object {
        fun fromDisplayName(name: String): PublicStateOption {
            return entries.find { it.displayName == name } ?: PRIVATE
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