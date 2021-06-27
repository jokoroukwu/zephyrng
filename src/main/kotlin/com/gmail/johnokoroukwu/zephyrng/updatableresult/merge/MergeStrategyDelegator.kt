package com.gmail.johnokoroukwu.zephyrng.updatableresult.merge

import com.gmail.johnokoroukwu.zephyrng.TestNgDataSetResult
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.gmail.johnokoroukwu.zephyrng.http.testresultstatus.TestResultStatus

class MergeStrategyDelegator(
    private val passedResultMergeStrategy: ResultMergeStrategy = PassedResultMergeStrategy,
    private val failedResultMergeStrategy: ResultMergeStrategy = FailedResultMergeStrategy
) : ResultMergeStrategy {

    override fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Int>,
        zephyrDataSet: List<ZephyrStepResult>,
        testNgDataSetResult: TestNgDataSetResult
    ): MergeResult {
        val strategy = if (testNgDataSetResult.isSuccess) passedResultMergeStrategy else failedResultMergeStrategy
        return strategy.mergeResults(
            testResultStatusToIdMap = testResultStatusToIdMap,
            zephyrDataSet = zephyrDataSet,
            testNgDataSetResult = testNgDataSetResult
        )
    }
}

