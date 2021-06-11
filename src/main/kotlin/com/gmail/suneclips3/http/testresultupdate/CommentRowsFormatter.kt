package com.gmail.suneclips3.http.testresultupdate

import com.gmail.suneclips3.CommentRow
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus

object CommentRowsFormatter : ICommentRowFormatter {
    private const val lineBreak = "<br>"
    private const val boldTagOpen = "<strong>"
    private const val boldTagClose = "</strong>"

    private const val rgbTagOpenTemplate = """<span style=\"color: rgb(%d, %d, %d);\">"""
    private const val rgbTagClose = "</span>"


    private fun resolveColor(commentRow: CommentRow): String {
        return if (commentRow.status == TestResultStatus.PASS) {
            rgbTagOpenTemplate.format(0, 255, 0)
        } else {
            rgbTagOpenTemplate.format(255, 0, 0)
        }
    }

    override fun formatCommentRows(commentRows: Collection<CommentRow>): String {
        return commentRows.sorted().joinToString(
            separator = "$lineBreak$lineBreak",
            prefix = "${boldTagOpen}Test data results:$lineBreak$lineBreak",
            postfix = boldTagClose
        )
        { row -> "${resolveColor(row)}${row.index}) ${row.status}:$lineBreak${row.text}$rgbTagClose" }
    }
}