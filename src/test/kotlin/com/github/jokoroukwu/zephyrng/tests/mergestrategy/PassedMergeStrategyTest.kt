package com.github.jokoroukwu.zephyrng.tests.mergestrategy

import com.github.jokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import com.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.github.jokoroukwu.zephyrng.updatableresult.CommentRow
import com.github.jokoroukwu.zephyrng.updatableresult.merge.PassedResultMergeStrategy
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.Test
import java.util.*

class PassedMergeStrategyTest {
    private val passedStatusId = 0L
    private val statusToIdMap = EnumMap<TestResultStatus, Long>(TestResultStatus::class.java).apply {
        put(TestResultStatus.PASS, passedStatusId)
    }
    private val dataSetIndex = 1
    private val testNgDataSetResult = com.github.jokoroukwu.zephyrng.TestNgDataSetResult(20L, 30, dataSetIndex, true)


    @Test
    fun `should return expected merge result for empty script results`() {
        val actualMergeResult = PassedResultMergeStrategy.mergeResults(statusToIdMap, emptyList(), testNgDataSetResult)

        val expectedCommentRow = CommentRow(testNgDataSetResult.index, TestResultStatus.PASS)
        val softly = SoftAssertions()
        softly.assertThat(actualMergeResult.testScriptResults).`as`("test script results")
            .isEqualTo(emptyList<TestScriptResult>())

        softly.assertThat(actualMergeResult.commentRow).`as`("comment row")
            .isEqualTo(expectedCommentRow)

        softly.assertThat(actualMergeResult.error).`as`("error")
            .isNotEmpty

        softly.assertAll()
    }

    @Test
    fun `should return expected merge result for non empty script results`() {
        val scriptResultIdOne = 0L
        val scriptResultIdTwo = 1L
        val expectedScriptResults = listOf(
            TestScriptResult(scriptResultIdOne, passedStatusId),
            TestScriptResult(scriptResultIdTwo, passedStatusId)
        )
        val detailedReportTestScriptResults =
            listOf(
                ZephyrStepResult(scriptResultIdOne, scriptResultIdOne.toInt()),
                ZephyrStepResult(scriptResultIdTwo, scriptResultIdTwo.toInt())
            )
        val actualMergeResult =
            PassedResultMergeStrategy.mergeResults(statusToIdMap, detailedReportTestScriptResults, testNgDataSetResult)

        val softly = SoftAssertions()
        softly.assertThat(actualMergeResult.testScriptResults).`as`("test script results")
            .containsExactlyInAnyOrderElementsOf(expectedScriptResults)

        softly.assertThat(actualMergeResult.commentRow).`as`("comment row")
            .isNull()

        softly.assertThat(actualMergeResult.error).`as`("error")
            .isNull()

        softly.assertAll()
    }
}