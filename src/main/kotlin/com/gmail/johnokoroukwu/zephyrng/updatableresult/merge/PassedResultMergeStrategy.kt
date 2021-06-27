package com.gmail.johnokoroukwu.zephyrng.updatableresult.merge

import com.gmail.johnokoroukwu.zephyrng.TestNgDataSetResult
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.gmail.johnokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.gmail.johnokoroukwu.zephyrng.updatableresult.CommentRow

object PassedResultMergeStrategy : ResultMergeStrategy {

    override fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Int>,
        zephyrDataSet: List<ZephyrStepResult>,
        testNgDataSetResult: TestNgDataSetResult
    ): MergeResult {

        return if (zephyrDataSet.isEmpty()) {
            val warning =
                "Data set result will be displayed in comment field: " +
                        "data set index out of bounds: {index: ${testNgDataSetResult.index}}"
            MergeResult(emptyList(), CommentRow(testNgDataSetResult.index, TestResultStatus.PASS), warning)
        } else {
            val passedStatusId = testResultStatusToIdMap.getValue(TestResultStatus.PASS)
            ArrayList<TestScriptResult>(zephyrDataSet.size).let { list ->
                zephyrDataSet.forEach {
                    list.add(TestScriptResult(it.id, passedStatusId))
                }
                MergeResult(list)
            }
        }
    }
}
