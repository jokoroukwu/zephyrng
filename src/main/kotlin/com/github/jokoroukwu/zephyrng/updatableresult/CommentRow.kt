package com.github.jokoroukwu.zephyrng.updatableresult

import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus

/**
 * An indexed text row in the "Comment" field of Zephyr test result.
 * Used to update test result when it is impossible to do so by steps,
 * either because the failed step index is out of bounds or no [com.gmail.johnokoroukwu.zephyrng.annotations.ZephyrStep]
 * annotations were found at all.
 *
 */
data class CommentRow(val index: Int, val status: TestResultStatus, val text: String = "") : Comparable<CommentRow> {

    override fun compareTo(other: CommentRow) = index.compareTo(other.index)

}