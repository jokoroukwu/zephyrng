package com.gmail.johnokoroukwu.zephyrng.updatableresult.merge

import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import com.gmail.johnokoroukwu.zephyrng.updatableresult.CommentRow

data class MergeResult(
    val testScriptResults: Collection<TestScriptResult> = emptyList(),
    val commentRow: CommentRow? = null,
    val error: String? = null
)