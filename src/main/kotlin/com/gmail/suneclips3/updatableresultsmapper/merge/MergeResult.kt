package com.gmail.suneclips3.updatableresultsmapper.merge

import com.gmail.suneclips3.CommentRow
import com.gmail.suneclips3.http.detailedreport.TestScriptResult

data class MergeResult(
    val testScriptResults: Collection<TestScriptResult> = emptyList(),
    val commentRow: CommentRow? = null,
    val error: String? = null
)