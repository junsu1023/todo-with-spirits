package com.example.todowithspirits.feature.forest

internal data class ForestHostRequest(
    val kind: String,
    val accountId: String,
    val date: String,
    val requestId: String
)

/** A consumed or superseded request can never change the current native curtain. */
internal object ForestHostReplyPolicy {
    fun accepts(expected: ForestHostRequest?, received: ForestHostRequest, status: String): Boolean {
        if (expected == null || expected.accountId.isBlank() || expected.date.isBlank() ||
            expected.requestId.isBlank() || expected != received) return false
        return when (expected.kind) {
            "host-session" -> status == "session-ready" || status == "session-rejected"
            "todo-snapshot" -> status == "applied" || status == "duplicate" || status == "rejected"
            "todo-sync" -> status == "status-loading" || status == "status-offline" || status == "status-error" || status == "rejected"
            else -> false
        }
    }
}
