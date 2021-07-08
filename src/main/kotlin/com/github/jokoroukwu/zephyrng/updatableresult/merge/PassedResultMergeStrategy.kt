package com.github.jokoroukwu.zephyrng.updatableresult.merge

import com.github.jokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import com.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.github.jokoroukwu.zephyrng.updatableresult.CommentRow

object PassedResultMergeStrategy : ResultMergeStrategy {

    override fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Long>,
        zephyrDataSet: List<ZephyrStepResult>,
        testNgDataSetResult: com.github.jokoroukwu.zephyrng.TestNgDataSetResult
    ): MergeResult {

        return if (zephyrDataSet.isEmpty()) {
            val warning =
                "Data set result will be displayed in comment field: " +
                        "data set index out of bounds: {index: ${testNgDataSetResult.index}}"
            MergeResult(
                testScriptResults = emptyList(),
                commentRow = CommentRow(testNgDataSetResult.index, TestResultStatus.PASS),
                error = warning,
                status = TestResultStatus.PASS
            )
        } else {
            val passedStatusId = testResultStatusToIdMap.getValue(TestResultStatus.PASS)
            ArrayList<TestScriptResult>(zephyrDataSet.size).let { list ->
                zephyrDataSet.forEach {
                    list.add(TestScriptResult(it.id, passedStatusId))
                }
                MergeResult(testScriptResults = list, status = TestResultStatus.PASS)
            }
        }
    }
}
