package com.example.data.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiErrorMessageTranslatorTest {
    @Test fun translatesExactMessageMatch() {
        assertEquals(
            "잘못된 이메일 형식입니다.",
            ApiErrorMessageTranslator.translate("Invalid email format")
        )
        assertEquals(
            "매일 반복 일정은 미룰 수 없습니다.",
            ApiErrorMessageTranslator.translate("Daily tasks cannot change date, only time")
        )
    }

    @Test fun trimsSurroundingWhitespaceBeforeMatching() {
        assertEquals(
            "제목을 입력하세요.",
            ApiErrorMessageTranslator.translate("  Title is required  ")
        )
    }

    @Test fun translatesPrefixOnlyMessagesThatCarryVariableSuffixes() {
        assertEquals(
            "이미 완료된 일정입니다.",
            ApiErrorMessageTranslator.translate("Task already completed on 2026-01-01")
        )
        assertEquals(
            "유효하지 않은 날짜입니다.",
            ApiErrorMessageTranslator.translate("Invalid date. 2026-01-01 is not an occurrence date for this task.")
        )
    }

    @Test fun fallsBackToErrorCodeWhenMessageHasNoMapping() {
        assertEquals("만료된 토큰입니다.", ApiErrorMessageTranslator.translate(null, "REVOKED_TOKEN"))
        assertEquals(
            "이미 가입된 이메일입니다.",
            ApiErrorMessageTranslator.translate("some unmapped server text", "DUPLICATE_EMAIL")
        )
    }

    @Test fun returnsNullWhenNeitherMessageNorCodeMatch() {
        assertNull(ApiErrorMessageTranslator.translate("totally unknown message", "SOME_UNKNOWN_CODE"))
        assertNull(ApiErrorMessageTranslator.translate(null, null))
    }
}
