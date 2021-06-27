package com.gmail.johnokoroukwu.zephyrng.updatableresult.merge

import com.gmail.johnokoroukwu.zephyrng.TestNgDataSetResult
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.gmail.johnokoroukwu.zephyrng.http.testresultstatus.TestResultStatus

interface ResultMergeStrategy {

    fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Int>,
        zephyrDataSet: List<ZephyrStepResult>,
        testNgDataSetResult: TestNgDataSetResult
    ): MergeResult
}