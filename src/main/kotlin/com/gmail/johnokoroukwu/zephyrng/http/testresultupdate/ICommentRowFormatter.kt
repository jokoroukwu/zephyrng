package com.gmail.johnokoroukwu.zephyrng.http.testresultupdate

import com.gmail.johnokoroukwu.zephyrng.updatableresult.CommentRow

interface ICommentRowFormatter {

    /**
     * Transforms provided comment row collection to desirable string representation
     * @return string representation of comment rows
     *
     * @see CommentRowColorFormatter
     */
    fun formatCommentRow(commentRows: Collection<CommentRow>): String

}