package com.github.jokoroukwu.zephyrng.tests.mergestrategy

import com.github.jokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import com.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.github.jokoroukwu.zephyrng.updatableresult.CommentRow
import com.github.jokoroukwu.zephyrng.updatableresult.merge.FailedResultMergeStrategy
import com.github.jokoroukwu.zephyrng.updatableresult.merge.MergeResult
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.*

class FailedMergeStrategyTest {
    private val failedResultId = 1L
    private val passedResultId = 2L
    private val blockedResultId = 3L
    private val message = "test-message"

    private val testResultStatusToId = EnumMap<TestResultStatus, Long>(TestResultStatus::class.java).apply {
        put(TestResultStatus.FAIL, failedResultId)
        put(TestResultStatus.PASS, passedResultId)
        put(TestResultStatus.BLOCKED, blockedResultId)
    }

    @Test(dataProvider = "shouldReturnExpectedCommentRowProvider")
    fun `should return expected comment row`(
        scriptResults: List<ZephyrStepResult>,
        testNgResults: com.github.jokoroukwu.zephyrng.TestNgDataSetResult,
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

        val emptyResults = emptyList<ZephyrStepResult>()
        val testNgDataSetForEmptyResults =
            com.github.jokoroukwu.zephyrng.TestNgDataSetResult(
                startTime,
                endTime,
                dataSetIndex,
                false,
                failedStepIndex,
                message
            )
        val commentRowForEmptyResults = CommentRow(dataSetIndex, TestResultStatus.FAIL, message)
        val mergeResultForEmptyResult = MergeResult(
            testScriptResults = emptyList(),
            commentRow = commentRowForEmptyResults,
            status = TestResultStatus.FAIL
        )

        val outOfBoundResults = listOf(TestScriptResult(0, failedStatusId))
        val testNgDataSetForOutOfBoundsResults =
            com.github.jokoroukwu.zephyrng.TestNgDataSetResult(
                startTime,
                endTime,
                dataSetIndex,
                false,
                failedStepIndex,
                message
            )
        val commentRowForOutOfBoundResults = CommentRow(dataSetIndex, TestResultStatus.FAIL, message)
        val outOfBoundsMergeResult = MergeResult(
            testScriptResults = emptyList(),
            commentRow = commentRowForOutOfBoundResults,
            status = TestResultStatus.FAIL
        )

        return arrayOf(
            arrayOf(emptyResults, testNgDataSetForEmptyResults, outOfBoundsMergeResult),
            arrayOf(outOfBoundResults, testNgDataSetForOutOfBoundsResults, mergeResultForEmptyResult)
        )
    }

    @Test(dataProvider = "testScriptResultsTestProvider")
    fun `should return expected test script results`(
        scriptResults: List<ZephyrStepResult>,
        testNgDataSetResult: com.github.jokoroukwu.zephyrng.TestNgDataSetResult,
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
            ZephyrStepResult(0, 0),
            ZephyrStepResult(1, 1),
            ZephyrStepResult(2, 2)
        )
        val allStatusTestNgDataSet =
            com.github.jokoroukwu.zephyrng.TestNgDataSetResult(startTime, endTime, 0, false, 1, message)
        val expectedAllStatusMergeResult = MergeResult(
            testScriptResults = listOf(
                TestScriptResult(0, passedResultId),
                TestScriptResult(1, failedResultId, message),
                TestScriptResult(2, blockedResultId)
            ),
            status = TestResultStatus.FAIL
        )
        val noBlockedStatusTestNgDataSetResult =
            com.github.jokoroukwu.zephyrng.TestNgDataSetResult(startTime, endTime, 0, false, 2, message)
        val expectedNoBlockedStatusMergeResult = MergeResult(
            testScriptResults = listOf(
                TestScriptResult(0, passedResultId),
                TestScriptResult(1, passedResultId),
                TestScriptResult(2, failedResultId, message)
            ),
            status = TestResultStatus.FAIL
        )
        val noPassedStatusTestNgDataSetResult =
            com.github.jokoroukwu.zephyrng.TestNgDataSetResult(startTime, endTime, 0, false, 0, message)
        val expectedNoPassedStatusMergeResult = MergeResult(
            testScriptResults = listOf(
                TestScriptResult(0, failedResultId, message),
                TestScriptResult(1, blockedResultId),
                TestScriptResult(2, blockedResultId)
            ),
            status = TestResultStatus.FAIL
        )
        return arrayOf(
            arrayOf(scriptResults, allStatusTestNgDataSet, expectedAllStatusMergeResult),
            arrayOf(scriptResults, noBlockedStatusTestNgDataSetResult, expectedNoBlockedStatusMergeResult),
            arrayOf(scriptResults, noPassedStatusTestNgDataSetResult, expectedNoPassedStatusMergeResult)
        )
    }
}