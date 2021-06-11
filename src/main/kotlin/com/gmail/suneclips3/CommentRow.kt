package com.gmail.suneclips3

import com.gmail.suneclips3.http.testresultstatus.TestResultStatus

data class CommentRow(val index: Int, val status: TestResultStatus, val text: String = "") : Comparable<CommentRow> {

    override fun compareTo(other: CommentRow) = index.compareTo(other.index);

}