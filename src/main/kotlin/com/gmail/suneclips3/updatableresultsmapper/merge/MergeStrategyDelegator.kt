package com.gmail.suneclips3.updatableresultsmapper.merge

import com.gmail.suneclips3.TestNgDataSetResult
import com.gmail.suneclips3.http.detailedreport.DetailedReportTestScriptResult
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus

class MergeStrategyDelegator(
    private val dataSetStatusToStrategy: Map<Boolean, IResultMergeStrategy>
) : IResultMergeStrategy {

    override fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Int>,
        zephyrDataSetSteps: List<DetailedReportTestScriptResult>,
        testNgDataSetResult: TestNgDataSetResult
    ) = dataSetStatusToStrategy.getValue(testNgDataSetResult.hasPassed)
        .mergeResults(
            testResultStatusToIdMap = testResultStatusToIdMap,
            zephyrDataSetSteps = zephyrDataSetSteps,
            testNgDataSetResult = testNgDataSetResult
        )
}
