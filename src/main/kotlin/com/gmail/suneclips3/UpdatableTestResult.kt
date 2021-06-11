package com.gmail.suneclips3

import com.gmail.suneclips3.http.testresultstatus.TestResultStatus
import java.time.Instant

class UpdatableTestResult(
    val startTime: Instant,
    val endTime: Instant,
    val testResultId: Int,
    val commentRows: List<CommentRow>
) {

    fun getEffectiveStatus(): TestResultStatus {
        return if (commentRows.any { it.status == TestResultStatus.FAIL }) {
            TestResultStatus.FAIL
        } else TestResultStatus.PASS
    }
}