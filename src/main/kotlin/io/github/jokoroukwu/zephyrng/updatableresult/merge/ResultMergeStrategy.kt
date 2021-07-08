package io.github.jokoroukwu.zephyrng.updatableresult.merge

import io.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import io.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus

interface ResultMergeStrategy {

    fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Long>,
        zephyrDataSet: List<ZephyrStepResult>,
        testNgDataSetResult: io.github.jokoroukwu.zephyrng.TestNgDataSetResult
    ): MergeResult
}