package com.gmail.suneclips3.tests.mergestrategy

import com.gmail.suneclips3.CommentRow
import com.gmail.suneclips3.TestNgDataSetResult
import com.gmail.suneclips3.http.detailedreport.DetailedReportTestScriptResult
import com.gmail.suneclips3.http.detailedreport.TestScriptResult
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus
import com.gmail.suneclips3.updatableresultsmapper.merge.PassedResultMergeStrategy
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.Test
import java.util.*

class PassedMergeStrategyTest {
    private val passedStatusId = 0
    private val statusToIdMap = EnumMap<TestResultStatus, Int>(TestResultStatus::class.java).apply {
        put(TestResultStatus.PASS, passedStatusId)
    }
    private val dataSetIndex = 1
    private val testNgDataSetResult = TestNgDataSetResult(20L, 30, dataSetIndex, true)


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
        val scriptResultIdOne = 0
        val scriptResultIdTwo = 1
        val expectedScriptResults = listOf(
            TestScriptResult(scriptResultIdOne, passedStatusId),
            TestScriptResult(scriptResultIdTwo, passedStatusId)
        )
        val detailedReportTestScriptResults =
            listOf(
                DetailedReportTestScriptResult(scriptResultIdOne, scriptResultIdOne),
                DetailedReportTestScriptResult(scriptResultIdTwo, scriptResultIdTwo)
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