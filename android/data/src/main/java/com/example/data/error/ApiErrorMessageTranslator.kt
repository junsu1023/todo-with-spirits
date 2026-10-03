package com.example.data.error

// 서버가 영어로 내려주는 에러 메시지(및 일부 errorCode)를 사용자에게 보여줄 한국어 문구로
// 매핑한다. apiCall/apiCallUnit에서 ApiException을 만들 때 한 번만 거치면, 이후
// ApiException.message / fieldErrors를 쓰는 모든 ViewModel은 번역 여부를 몰라도 된다.
object ApiErrorMessageTranslator {
    // 메시지 원문(공백 트림 후) 완전 일치 매핑.
    private val messageTranslations: Map<String, String> = mapOf(
        "Invalid email format" to "잘못된 이메일 형식입니다.",
        "email is required" to "이메일을 입력하세요.",
        "Invalid email or password" to "이메일 또는 비밀번호가 올바르지 않습니다.",
        "Authentication required" to "유효하지 않은 토큰입니다.",
        "Password must contain at least one letter and one number" to "비밀번호에 문자와 숫자가 포함되어야 합니다.",
        "Password must be between 8 and 20 characters" to "비밀번호는 8글자 이상 20글자 이하여야 합니다.",
        "Email already in use" to "이미 가입된 이메일입니다.",
        "Provider is required" to "Provider 누락",
        "Provider User Id is required" to "userId 누락",
        "Code is required" to "인증 번호를 입력하세요.",
        "An account with this email already exists" to "이미 존재하는 계정입니다.",
        "Nickname must be between 2 and 12 characters" to "닉네임은 2~12글자여야 합니다.",
        "Request body is missing or contains an invalid value" to "요청 본문 누락 혹은 유효하지 않은 값 포함",
        "Spirit not found" to "정령을 찾을 수 없습니다.",
        "User not found" to "사용자 정보를 찾을 수 없습니다.",
        "Cannot query more than 90 days of calendar data" to "90일동안의 데이터만 확인할 수 있습니다.",
        "Invalid or missing credential" to "유효하지 않거나 누락된 정보가 있습니다.",
        "date is required" to "유효한 날짜가 아닙니다.",
        "Notification not found" to "유효하지 않은 알림입니다.",
        "invalid cursor" to "유효하지 않은 알림입니다.",
        "Spirit ID to change is required" to "변경할 정령ID가 필요합니다.",
        "taskIds must not be empty" to "일정이 존재하지 않습니다.",
        "Not found schedule or routine" to "일정이나 루틴을 찾을 수 없습니다.",
        "Not found schedule or routine." to "일정이나 루틴을 찾을 수 없습니다.",
        "originalDate is required for routines" to "루틴을 미루기 위해서는 기존 일자가 필요합니다.",
        "Not an occurrence date for this routine" to "루틴의 생성 일자가 잘못되었습니다.",
        "Postpone limit reached for this occurrence (max 3)" to "최대 미루기 횟수 한도에 도달했습니다. (최대 3회)",
        "Daily routines cannot change date, only time" to "일일 루틴은 미룰 수 없습니다.",
        "Weekly routines can only be postponed within the current week" to "주간 루틴은 해당 주 내에서만 연기할 수 있습니다.",
        "Monthly routines can only be postponed within the current month" to "월간 루틴은 해당 월에서만 연기할 수 있습니다.",
        "Unsupported repeat type for postpone" to "미룰 수 없는 일정/루틴입니다.",
        "Target date already has an occurrence of this routine" to "미룰 날짜에 같은 루틴이 있습니다.",
        "Target date is already occupied by another occurrence of this routine" to "미룰 날짜에 같은 루틴이 있습니다.",
        "Routine repeat type must be DAILY, WEEKLY, or MONTHLY" to "루틴은 일간, 주간, 월간 중 하나여야 합니다.",
        "Days of week are required for weekly repeat" to "매주 반복을 위한 요일을 선택하세요.",
        "Days of month are required for monthly repeat" to "매월 반복을 위한 날짜를 선택하세요.",
        "Day of month must be between 1 and 31" to "일자는 1~31일 사이여야 합니다.",
        "Title is required" to "제목을 입력하세요.",
        "Repeat type is required" to "반복 유형은 필수 항목입니다.",
        "Habit repeat type must be DAILY, WEEKLY, or MONTHLY" to "루틴은 일간, 주간, 월간 중 하나여야 합니다.",
        "Task is not a routine" to "일정id로 루틴 생성 시도 - 개발용",
        "isAllDay is required" to "isAllDay는 필수 항목입니다.",
        "endDateTime is required" to "종료일자는 필수 항목입니다.",
        "Task is not a schedule" to "루틴id로 일정 생성 시도 - 개발용"
    )

    // 뒤에 가변 값(날짜 등)이 붙어 완전 일치가 안 되는 메시지를 위한 접두사 매핑.
    private val prefixTranslations: List<Pair<String, String>> = listOf(
        "Invalid routine date." to "유효하지 않은 날짜입니다.",
        "Routine already completed on" to "이미 완료된 루틴입니다."
    )

    // message로 못 찾으면 errorCode로 한 번 더 시도한다(메시지 없이 코드만 내려오는 경우 대비).
    private val codeTranslations: Map<String, String> = mapOf(
        "REVOKED_TOKEN" to "만료된 토큰입니다.",
        "INVALID_TOKEN" to "유효하지 않은 토큰입니다.",
        "INVALID_PROVIDER_TOKEN" to "유효하지 않은 토큰입니다.",
        "DUPLICATE_EMAIL" to "이미 가입된 이메일입니다.",
        "NOT_FOUND" to "일정이나 루틴을 찾을 수 없습니다.",
        "UNAUTHORIZED" to "권한 없음"
    )

    // 매핑을 못 찾으면 null — 호출부가 원문 메시지 등 자체 폴백을 쓸 수 있게 둔다.
    fun translate(message: String?, errorCode: String? = null): String? {
        val trimmed = message?.trim()
        if (!trimmed.isNullOrEmpty()) {
            messageTranslations[trimmed]?.let { return it }
            prefixTranslations.firstOrNull { trimmed.startsWith(it.first) }?.let { return it.second }
        }

        return errorCode?.let { codeTranslations[it] }
    }
}
