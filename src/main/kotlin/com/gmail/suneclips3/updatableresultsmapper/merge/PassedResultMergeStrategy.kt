package com.gmail.suneclips3.updatableresultsmapper.merge

import com.gmail.suneclips3.CommentRow
import com.gmail.suneclips3.TestNgDataSetResult
import com.gmail.suneclips3.http.detailedreport.DetailedReportTestScriptResult
import com.gmail.suneclips3.http.detailedreport.TestScriptResult
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus

object PassedResultMergeStrategy : IResultMergeStrategy {

    override fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Int>,
        zephyrDataSetSteps: List<DetailedReportTestScriptResult>,
        testNgDataSetResult: TestNgDataSetResult
    ): MergeResult {

        return if (zephyrDataSetSteps.isEmpty()) {
            val warning =
                "data set ${testNgDataSetResult.index} result will be displayed in comment field: " +
                        "data set index out of bounds"
            MergeResult(emptyList(), CommentRow(testNgDataSetResult.index, TestResultStatus.PASS), warning)
        } else {
            val passedStatusId = testResultStatusToIdMap.getValue(TestResultStatus.PASS)
            ArrayList<TestScriptResult>(zephyrDataSetSteps.size).let { list ->
                zephyrDataSetSteps.forEach {
                    list.add(TestScriptResult(it.id, passedStatusId))
                }
                MergeResult(list)
            }
        }
    }
}
