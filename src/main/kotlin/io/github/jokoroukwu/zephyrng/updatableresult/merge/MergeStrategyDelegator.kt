package io.github.jokoroukwu.zephyrng.updatableresult.merge

import io.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import io.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus

class MergeStrategyDelegator(
    private val passedResultMergeStrategy: ResultMergeStrategy = PassedResultMergeStrategy,
    private val failedResultMergeStrategy: ResultMergeStrategy = FailedResultMergeStrategy
) : ResultMergeStrategy {

    override fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Long>,
        zephyrDataSet: List<ZephyrStepResult>,
        testNgDataSetResult: io.github.jokoroukwu.zephyrng.TestNgDataSetResult
    ): MergeResult {
        val strategy = if (testNgDataSetResult.isSuccess) passedResultMergeStrategy else failedResultMergeStrategy
        return strategy.mergeResults(
            testResultStatusToIdMap = testResultStatusToIdMap,
            zephyrDataSet = zephyrDataSet,
            testNgDataSetResult = testNgDataSetResult
        )
    }
}

