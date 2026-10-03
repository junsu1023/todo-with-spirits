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
            "루틴을 미루기 위해서는 기존 일자가 필요합니다.",
            ApiErrorMessageTranslator.translate("originalDate is required for routines")
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
            "이미 완료된 루틴입니다.",
            ApiErrorMessageTranslator.translate("Routine already completed on 2026-01-01")
        )
        assertEquals(
            "유효하지 않은 날짜입니다.",
            ApiErrorMessageTranslator.translate("Invalid routine date. must be after today")
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
