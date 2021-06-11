package com.gmail.suneclips3.http.testresultupdate

import com.gmail.suneclips3.CommentRow

interface ICommentRowFormatter {

    fun formatCommentRows(commentRows: Collection<CommentRow>): String
}