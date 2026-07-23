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

enum class CategoryOption(val displayName: String) {
    NONE("미정"),
    WORK_STUDY("업무/학업/커리어/공부"),
    HEALTH("건강"),
    LIFE("생활"),
    RELATIONSHIP("인간관계/약속"),
    GROWTH("자기계발"),
    HOBBY("취미"),
    REST("휴식/마인드"),
    FINANCE("자산/경제");

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