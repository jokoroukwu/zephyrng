package com.gmail.johnokoroukwu.zephyrng.http.testresultupdate

import com.gmail.johnokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.gmail.johnokoroukwu.zephyrng.updatableresult.CommentRow

object CommentRowColorFormatter : ICommentRowFormatter {
    const val LINE_BREAK = "<br>"

    const val BOLD_TAG_OPEN = "<strong>"
    const val BOLD_TAG_CLOSE = "</strong>"

    const val RGB_TAG_OPEN_TEMPLATE = """<span style=\"color: rgb(%d, %d, %d);\">"""
    const val RGB_TAG_CLOSE = "</span>"

    val FAILED_COLOR_OPEN = RGB_TAG_OPEN_TEMPLATE.format(160, 0, 0)
    val PASSED_COLOR_OPEN = RGB_TAG_OPEN_TEMPLATE.format(0, 100, 50)

    /**
     * Transforms comment rows into a bulletin list where each row
     * begins at new line, prefixed by its index.
     * The provided collection should be sorted to preserve the order
     *
     * Rows that represent passed results are colored in green,
     * whereas those that represent failed results are in red
     *
     * @return a string with meta information for proper formatting in Zephyr
     */
    override fun formatCommentRow(commentRows: Collection<CommentRow>) =
        commentRows.joinToString(
            prefix = "${BOLD_TAG_OPEN}Data sets results:$LINE_BREAK$LINE_BREAK",
            separator = "$LINE_BREAK$LINE_BREAK",
            postfix = BOLD_TAG_CLOSE,
            transform = ::rowToString
        )

    private fun rowToString(row: CommentRow): String {
        val rgbColorTag = if (row.status == TestResultStatus.PASS) PASSED_COLOR_OPEN else FAILED_COLOR_OPEN
        val messagePart = if (row.text.isNotEmpty()) ":$LINE_BREAK${row.text}" else ""
        return "$rgbColorTag${row.index}) ${row.status}$messagePart$RGB_TAG_CLOSE"
    }
}