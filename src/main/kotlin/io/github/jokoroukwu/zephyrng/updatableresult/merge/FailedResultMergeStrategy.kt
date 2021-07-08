package io.github.jokoroukwu.zephyrng.updatableresult.merge

import io.github.jokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import io.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import io.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import io.github.jokoroukwu.zephyrng.updatableresult.CommentRow

object FailedResultMergeStrategy : ResultMergeStrategy {
    override fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Long>,
        zephyrDataSet: List<ZephyrStepResult>,
        testNgDataSetResult: io.github.jokoroukwu.zephyrng.TestNgDataSetResult
    ): MergeResult {

        when {
            zephyrDataSet.isEmpty() -> {
                val warning = "Data set result will be displayed in comment field: " +
                        "data set index out of bounds: {index: ${testNgDataSetResult.index}}"
                val commentRow =
                    CommentRow(testNgDataSetResult.index, TestResultStatus.FAIL, testNgDataSetResult.failureMessage)
                return MergeResult(commentRow = commentRow, error = warning, status = TestResultStatus.FAIL)
            }

            testNgDataSetResult.failedStepIndex == null -> {
                val commentRow =
                    CommentRow(testNgDataSetResult.index, TestResultStatus.FAIL, testNgDataSetResult.failureMessage)
                return MergeResult(commentRow = commentRow, status = TestResultStatus.FAIL)
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
                        id = zephyrDataSet[failedIndex].id,
                        testResultStatusId = failedStatusId,
                        comment = testNgDataSetResult.failureMessage
                    )
                )
                //  now mark all steps following failed one as BLOCKED
                val blockedRange = failedIndex + 1 until zephyrDataSet.size
                addScriptResults(zephyrDataSet, blockedStatusId, testScriptResults, blockedRange)

                return MergeResult(testScriptResults, status = TestResultStatus.FAIL)
            }
            else -> {
                val warning = "Data set result will be displayed in comment field: " +
                        "failed step index is out of bounds: {dataset_index: ${testNgDataSetResult.index}, " +
                        "failed_step_index: ${testNgDataSetResult.failedStepIndex}}"
                val commentRow =
                    CommentRow(testNgDataSetResult.index, TestResultStatus.FAIL, testNgDataSetResult.failureMessage)
                return MergeResult(commentRow = commentRow, error = warning, status = TestResultStatus.FAIL)
            }
        }
    }

    private fun addScriptResults(
        fetchedDataset: List<ZephyrStepResult>,
        statusId: Long,
        testScriptResult: MutableList<TestScriptResult>,
        range: IntRange
    ) {
        for (i in range) {
            fetchedDataset[i]
            testScriptResult.add(
                TestScriptResult(
                    id = fetchedDataset[i].id,
                    testResultStatusId = statusId
                )
            )
        }
    }
}