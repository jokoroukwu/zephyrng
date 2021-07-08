package com.github.jokoroukwu.zephyrng.updatableresult.merge

import com.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus

interface ResultMergeStrategy {

    fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Long>,
        zephyrDataSet: List<ZephyrStepResult>,
        testNgDataSetResult: com.github.jokoroukwu.zephyrng.TestNgDataSetResult
    ): MergeResult
}