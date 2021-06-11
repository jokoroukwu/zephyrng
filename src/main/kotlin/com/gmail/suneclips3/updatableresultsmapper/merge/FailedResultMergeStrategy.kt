package com.gmail.suneclips3.updatableresultsmapper.merge

import com.gmail.suneclips3.CommentRow
import com.gmail.suneclips3.TestNgDataSetResult
import com.gmail.suneclips3.http.detailedreport.DetailedReportTestScriptResult
import com.gmail.suneclips3.http.detailedreport.TestScriptResult
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus

object FailedResultMergeStrategy : IResultMergeStrategy {
    override fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Int>,
        zephyrDataSetSteps: List<DetailedReportTestScriptResult>,
        testNgDataSetResult: TestNgDataSetResult
    ): MergeResult {

        when {
            zephyrDataSetSteps.isEmpty() -> {
                val warning =
                    "data set ${testNgDataSetResult.index} result will be displayed in comment field: " +
                            "data set index out of bounds"
                val commentRow =
                    CommentRow(testNgDataSetResult.index, TestResultStatus.FAIL, testNgDataSetResult.failureMessage)
                return MergeResult(commentRow = commentRow, error = warning)
            }

            testNgDataSetResult.failedStepIndex == null -> {
                val commentRow =
                    CommentRow(testNgDataSetResult.index, TestResultStatus.FAIL, testNgDataSetResult.failureMessage)
                return MergeResult(commentRow = commentRow)
            }

            testNgDataSetResult.failedStepIndex < zephyrDataSetSteps.size -> {
                val passedStatusId = testResultStatusToIdMap.getValue(TestResultStatus.PASS)
                val failedStatusId = testResultStatusToIdMap.getValue(TestResultStatus.FAIL)
                val blockedStatusId = testResultStatusToIdMap.getValue(TestResultStatus.BLOCKED)
                val testScriptResults = ArrayList<TestScriptResult>(zephyrDataSetSteps.size)

                val failedIndex = testNgDataSetResult.failedStepIndex
                // first mark all steps prior to failed one as PASSED
                val passedRange = 0 until failedIndex
                addScriptResults(zephyrDataSetSteps, passedStatusId, testScriptResults, passedRange)

                // then mark failed step as FAILED
                testScriptResults.add(
                    TestScriptResult(
                        zephyrDataSetSteps[failedIndex].id,
                        failedStatusId,
                        testNgDataSetResult.failureMessage
                    )
                )
                //  now mark all steps following failed one as BLOCKED
                val blockedRange = failedIndex + 1 until zephyrDataSetSteps.size
                addScriptResults(zephyrDataSetSteps, blockedStatusId, testScriptResults, blockedRange)

                return MergeResult(testScriptResults)
            }
            else -> {
                val warning = "data set ${testNgDataSetResult.index} result will be displayed in comment field: " +
                        "failed step index ${testNgDataSetResult.failedStepIndex} out of bounds"
                val commentRow =
                    CommentRow(testNgDataSetResult.index, TestResultStatus.FAIL, testNgDataSetResult.failureMessage)
                return MergeResult(commentRow = commentRow, error = warning)
            }
        }
    }

    private fun addScriptResults(
        fetchedDataset: List<DetailedReportTestScriptResult>,
        statusId: Int,
        testScriptResult: MutableList<TestScriptResult>,
        range: IntRange
    ) {
        for (i in range) {
            fetchedDataset[i]
            testScriptResult.add(TestScriptResult(fetchedDataset[i].id, statusId))
        }
    }
}