package com.github.jokoroukwu.zephyrng.updatableresult

import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import java.util.*

class UpdatableTestResult(
    val startTime: Long,
    val endTime: Long,
    val testResultId: Long,
    val commentRows: SortedSet<CommentRow>,
    val effectiveStatus: TestResultStatus
)