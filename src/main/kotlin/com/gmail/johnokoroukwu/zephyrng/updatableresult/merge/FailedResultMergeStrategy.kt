package com.gmail.johnokoroukwu.zephyrng.updatableresult.merge

import com.gmail.johnokoroukwu.zephyrng.TestNgDataSetResult
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.gmail.johnokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.gmail.johnokoroukwu.zephyrng.updatableresult.CommentRow

object FailedResultMergeStrategy : ResultMergeStrategy {
    override fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Int>,
        zephyrDataSet: List<ZephyrStepResult>,
        testNgDataSetResult: TestNgDataSetResult
    ): MergeResult {

        when {
            zephyrDataSet.isEmpty() -> {
                val warning = "Data set result will be displayed in comment field: " +
                        "data set index out of bounds: {index: ${testNgDataSetResult.index}}"
                val commentRow =
                    CommentRow(testNgDataSetResult.index, TestResultStatus.FAIL, testNgDataSetResult.failureMessage)
                return MergeResult(commentRow = commentRow, error = warning)
            }

            testNgDataSetResult.failedStepIndex == null -> {
                val commentRow =
                    CommentRow(testNgDataSetResult.index, TestResultStatus.FAIL, testNgDataSetResult.failureMessage)
                return MergeResult(commentRow = commentRow)
            }

            testNgDataSetResult.failedStepIndex < zephyrDataSet.size -> {
                val passedStatusId = testResultStatusToIdMap.getValue(TestResultStatus.PASS)
                val failedStatusId = testResultStatusToIdMap.getValue(TestResultStatus.FAIL)
                val blockedStatusId = testResultStatusToIdMap.getValue(TestResultStatus.BLOCKED)
                val testScriptResults = ArrayList<TestScriptResult>(zephyrDataSet.size)

                val failedIndex = testNgDataSetResult.failedStepIndex
                // first mark all steps prior to failed one as PASSED
                val passedRange = 0 until failedIndex
                addScriptResults(zephyrDataSet, passedStatusId, testScriptResults, passedRange)

                // then mark failed step as FAILED
                testScriptResults.add(
                    TestScriptResult(
                        zephyrDataSet[failedIndex].id,
                        failedStatusId,
                        testNgDataSetResult.failureMessage
                    )
                )
                //  now mark all steps following failed one as BLOCKED
                val blockedRange = failedIndex + 1 until zephyrDataSet.size
                addScriptResults(zephyrDataSet, blockedStatusId, testScriptResults, blockedRange)

                return MergeResult(testScriptResults)
            }
            else -> {
                val warning = "Data set result will be displayed in comment field: " +
                        "failed step index is out of bounds: {dataset_index: ${testNgDataSetResult.index}, " +
                        "failed_step_index: ${testNgDataSetResult.failedStepIndex}}"
                val commentRow =
                    CommentRow(testNgDataSetResult.index, TestResultStatus.FAIL, testNgDataSetResult.failureMessage)
                return MergeResult(commentRow = commentRow, error = warning)
            }
        }
    }

    private fun addScriptResults(
        fetchedDataset: List<ZephyrStepResult>,
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