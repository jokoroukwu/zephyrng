package com.gmail.suneclips3.tests.mergestrategy

import com.gmail.suneclips3.CommentRow
import com.gmail.suneclips3.TestNgDataSetResult
import com.gmail.suneclips3.http.detailedreport.DetailedReportTestScriptResult
import com.gmail.suneclips3.http.detailedreport.TestScriptResult
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus
import com.gmail.suneclips3.updatableresultsmapper.merge.FailedResultMergeStrategy
import com.gmail.suneclips3.updatableresultsmapper.merge.MergeResult
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.*

class FailedMergeStrategyTest {
    private val failedResultId = 1
    private val passedResultId = 2
    private val blockedResultId = 3
    private val message = "test-message"

    private val testResultStatusToId = EnumMap<TestResultStatus, Int>(TestResultStatus::class.java).apply {
        put(TestResultStatus.FAIL, failedResultId)
        put(TestResultStatus.PASS, passedResultId)
        put(TestResultStatus.BLOCKED, blockedResultId)
    }

    @Test(dataProvider = "shouldReturnExpectedCommentRowProvider")
    fun `should return expected comment row`(
        scriptResults: List<DetailedReportTestScriptResult>,
        testNgResults: TestNgDataSetResult,
        expectedResult: MergeResult
    ) {
        val actualResult =
            FailedResultMergeStrategy.mergeResults(testResultStatusToId, scriptResults, testNgResults)
        val softly = SoftAssertions()

        softly.assertThat(actualResult.testScriptResults)
            .`as`("test script results")
            .isEqualTo(expectedResult.testScriptResults)

        softly.assertThat(actualResult.commentRow)
            .`as`("comment row")
            .isEqualTo(expectedResult.commentRow)

        softly.assertThat(actualResult.error).`as`("error").isNotEmpty

        softly.assertAll()
    }

    @DataProvider
    private fun shouldReturnExpectedCommentRowProvider(): Array<Array<Any>> {
        val startTime = 1L
        val endTime = 2L
        val dataSetIndex = 1
        val failedStepIndex = 1
        val failedStatusId = testResultStatusToId.getValue(TestResultStatus.FAIL)

        val emptyResults = emptyList<DetailedReportTestScriptResult>()
        val testNgDataSetForEmptyResults =
            TestNgDataSetResult(startTime, endTime, dataSetIndex, false, failedStepIndex, message)
        val commentRowForEmptyResults = CommentRow(dataSetIndex, TestResultStatus.FAIL, message)
        val mergeResultForEmptyResult = MergeResult(emptyList(), commentRowForEmptyResults)

        val outOfBoundResults = listOf(TestScriptResult(0, failedStatusId))
        val testNgDataSetForOutOfBoundsResults =
            TestNgDataSetResult(startTime, endTime, dataSetIndex, false, failedStepIndex, message)
        val commentRowForOutOfBoundResults = CommentRow(dataSetIndex, TestResultStatus.FAIL, message)
        val outOfBoundsMergeResult = MergeResult(emptyList(), commentRowForOutOfBoundResults)

        return arrayOf(
            arrayOf(emptyResults, testNgDataSetForEmptyResults, outOfBoundsMergeResult),
            arrayOf(outOfBoundResults, testNgDataSetForOutOfBoundsResults, mergeResultForEmptyResult)
        )
    }

    @Test(dataProvider = "testScriptResultsTestProvider")
    fun `should return expected test script results`(
        scriptResults: List<DetailedReportTestScriptResult>,
        testNgDataSetResult: TestNgDataSetResult,
        expectedResult: MergeResult
    ) {
        val actualResult =
            FailedResultMergeStrategy.mergeResults(testResultStatusToId, scriptResults, testNgDataSetResult)

        val softly = SoftAssertions()
        softly.assertThat(actualResult.commentRow).`as`("comment row").isEqualTo(expectedResult.commentRow)
        softly.assertThat(actualResult.error).`as`("error").isEqualTo(expectedResult.error)
        softly.assertThat(actualResult.testScriptResults)
            .`as`("test script results")
            .containsExactlyElementsOf(expectedResult.testScriptResults)

        softly.assertAll()
    }

    @DataProvider
    private fun testScriptResultsTestProvider(): Array<Array<Any>> {
        val startTime = 1L
        val endTime = 2L
        val scriptResults = listOf(
            DetailedReportTestScriptResult(0, 0),
            DetailedReportTestScriptResult(1, 1),
            DetailedReportTestScriptResult(2, 2)
        )
        val allStatusTestNgDataSet = TestNgDataSetResult(startTime, endTime, 0, false, 1, message)
        val expectedAllStatusMergeResult = MergeResult(
            listOf(
                TestScriptResult(0, passedResultId),
                TestScriptResult(1, failedResultId, message),
                TestScriptResult(2, blockedResultId)
            )
        )
        val noBlockedStatusTestNgDataSetResult = TestNgDataSetResult(startTime, endTime, 0, false, 2, message)
        val expectedNoBlockedStatusMergeResult = MergeResult(
            listOf(
                TestScriptResult(0, passedResultId),
                TestScriptResult(1, passedResultId),
                TestScriptResult(2, failedResultId, message)
            )
        )
        val noPassedStatusTestNgDataSetResult = TestNgDataSetResult(startTime, endTime, 0, false, 0, message)
        val expectedNoPassedStatusMergeResult = MergeResult(
            listOf(
                TestScriptResult(0, failedResultId, message),
                TestScriptResult(1, blockedResultId),
                TestScriptResult(2, blockedResultId)
            )
        )
        return arrayOf(
            arrayOf(scriptResults, allStatusTestNgDataSet, expectedAllStatusMergeResult),
            arrayOf(scriptResults, noBlockedStatusTestNgDataSetResult, expectedNoBlockedStatusMergeResult),
            arrayOf(scriptResults, noPassedStatusTestNgDataSetResult, expectedNoPassedStatusMergeResult)
        )
    }
}