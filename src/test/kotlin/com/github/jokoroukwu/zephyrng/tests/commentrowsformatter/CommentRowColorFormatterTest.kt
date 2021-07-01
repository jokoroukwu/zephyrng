package com.github.jokoroukwu.zephyrng.tests.commentrowsformatter

import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.github.jokoroukwu.zephyrng.http.testresultupdate.CommentRowColorFormatter
import com.github.jokoroukwu.zephyrng.http.testresultupdate.CommentRowColorFormatter.BOLD_TAG_CLOSE
import com.github.jokoroukwu.zephyrng.http.testresultupdate.CommentRowColorFormatter.BOLD_TAG_OPEN
import com.github.jokoroukwu.zephyrng.http.testresultupdate.CommentRowColorFormatter.FAILED_COLOR_OPEN
import com.github.jokoroukwu.zephyrng.http.testresultupdate.CommentRowColorFormatter.LINE_BREAK
import com.github.jokoroukwu.zephyrng.http.testresultupdate.CommentRowColorFormatter.PASSED_COLOR_OPEN
import com.github.jokoroukwu.zephyrng.http.testresultupdate.CommentRowColorFormatter.RGB_TAG_CLOSE
import com.github.jokoroukwu.zephyrng.updatableresult.CommentRow
import org.assertj.core.api.Assertions
import org.testng.annotations.Test

class CommentRowColorFormatterTest {


    @Test
    private fun `should return expected string`() {
        with(ArrayList<CommentRow>()) {
            val text = " some text"
            add(CommentRow(1, TestResultStatus.FAIL, text))
            add(CommentRow(2, TestResultStatus.PASS))

            val expectedPattern =
                """${BOLD_TAG_OPEN}Data sets results:${LINE_BREAK}${LINE_BREAK}${FAILED_COLOR_OPEN}1) FAIL:$LINE_BREAK$text$RGB_TAG_CLOSE$LINE_BREAK$LINE_BREAK${PASSED_COLOR_OPEN}2) PASS$RGB_TAG_CLOSE$BOLD_TAG_CLOSE"""
            Assertions.assertThat(CommentRowColorFormatter.formatCommentRow(this))
                .isEqualTo(expectedPattern)

        }

    }
}