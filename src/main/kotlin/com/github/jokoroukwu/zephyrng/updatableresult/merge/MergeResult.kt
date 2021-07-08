package com.github.jokoroukwu.zephyrng.updatableresult.merge

import com.github.jokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.github.jokoroukwu.zephyrng.updatableresult.CommentRow

data class MergeResult(
    val testScriptResults: Collection<TestScriptResult> = emptyList(),
    val commentRow: CommentRow? = null,
    val error: String? = null,
    val status: TestResultStatus
)