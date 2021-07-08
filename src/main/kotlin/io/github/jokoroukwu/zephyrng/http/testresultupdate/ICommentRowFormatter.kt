package io.github.jokoroukwu.zephyrng.http.testresultupdate

import io.github.jokoroukwu.zephyrng.updatableresult.CommentRow

interface ICommentRowFormatter {

    /**
     * Transforms provided comment row collection to desirable string representation
     * @return string representation of comment rows
     *
     * @see CommentRowColorFormatter
     */
    fun formatCommentRow(commentRows: Collection<CommentRow>): String

}