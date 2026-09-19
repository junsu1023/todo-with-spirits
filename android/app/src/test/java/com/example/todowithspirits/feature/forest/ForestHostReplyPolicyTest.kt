package com.example.todowithspirits.feature.forest

import org.junit.Assert.*
import org.junit.Test

class ForestHostReplyPolicyTest {
    private val session = ForestHostRequest("host-session", "account-b", "2026-09-18", "attempt-2")
    private val snapshot = session.copy(kind = "todo-snapshot", requestId = "snapshot-2")

    @Test fun matchingSessionSuccessAndFailureAreAccepted() {
        assertTrue(ForestHostReplyPolicy.accepts(session, session, "session-ready"))
        assertTrue(ForestHostReplyPolicy.accepts(session, session, "session-rejected"))
    }

    @Test fun priorAccountDateAndRetryCannotAcknowledgeOrRejectCurrentSession() {
        val stale = listOf(session.copy(accountId = "account-a"), session.copy(date = "2026-09-17"), session.copy(requestId = "attempt-1"))
        for (reply in stale) for (status in listOf("session-ready", "session-rejected"))
            assertFalse(ForestHostReplyPolicy.accepts(session, reply, status))
    }

    @Test fun olderSnapshotRejectionCannotBlockTheNewSnapshot() {
        assertFalse(ForestHostReplyPolicy.accepts(snapshot, snapshot.copy(requestId = "snapshot-1"), "rejected"))
        assertTrue(ForestHostReplyPolicy.accepts(snapshot, snapshot, "rejected"))
        assertTrue(ForestHostReplyPolicy.accepts(snapshot, snapshot, "applied"))
        assertTrue(ForestHostReplyPolicy.accepts(snapshot, snapshot, "duplicate"))
    }

    @Test fun statusAndSnapshotSupersedeEachOtherAcrossKinds() {
        val status = snapshot.copy(kind = "todo-sync", requestId = "status-1")
        assertFalse(ForestHostReplyPolicy.accepts(snapshot, status, "rejected"))
        assertFalse(ForestHostReplyPolicy.accepts(status, snapshot, "rejected"))
        assertTrue(ForestHostReplyPolicy.accepts(status, status, "status-loading"))
        assertTrue(ForestHostReplyPolicy.accepts(status, status, "status-offline"))
        assertTrue(ForestHostReplyPolicy.accepts(status, status, "status-error"))
    }

    @Test fun consumedMissingAndUnknownRepliesAreIgnored() {
        assertFalse(ForestHostReplyPolicy.accepts(null, session, "session-ready"))
        assertFalse(ForestHostReplyPolicy.accepts(snapshot, snapshot.copy(requestId = ""), "rejected"))
        assertFalse(ForestHostReplyPolicy.accepts(snapshot.copy(accountId = ""), snapshot.copy(accountId = ""), "applied"))
        assertFalse(ForestHostReplyPolicy.accepts(snapshot, snapshot, "session-ready"))
        assertFalse(ForestHostReplyPolicy.accepts(session, session, "rejected"))
        assertFalse(ForestHostReplyPolicy.accepts(snapshot, snapshot, "unknown"))
    }
}
