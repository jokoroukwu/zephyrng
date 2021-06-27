package com.gmail.johnokoroukwu.zephyrng.updatableresult

import com.gmail.johnokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import java.util.*

class UpdatableTestResult(
    val startTime: Long,
    val endTime: Long,
    val testResultId: Int,
    val commentRows: SortedSet<CommentRow>
) {

    fun getEffectiveStatus(): TestResultStatus {
        return if (commentRows.any { it.status == TestResultStatus.FAIL }) {
            TestResultStatus.FAIL
        } else TestResultStatus.PASS
    }
}