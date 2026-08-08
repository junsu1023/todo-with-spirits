package com.example.todowithspirits.util

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

internal fun isValidEmail(email: String) = EMAIL_REGEX.matches(email)

internal fun isValidPasswordFormat(password: String): Boolean {
    if (password.isEmpty() || password.length < 8) return false

    val matchedTypeCount = listOf(
        password.any { it.isLetter() },
        password.any { it.isDigit() },
        password.any { !it.isLetterOrDigit() }
    ).count { it }

    return matchedTypeCount >= 2
}
