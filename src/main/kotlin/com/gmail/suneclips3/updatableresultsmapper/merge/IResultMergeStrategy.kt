package com.gmail.suneclips3.updatableresultsmapper.merge

import com.gmail.suneclips3.TestNgDataSetResult
import com.gmail.suneclips3.http.detailedreport.DetailedReportTestScriptResult
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus

interface IResultMergeStrategy {

    fun mergeResults(
        testResultStatusToIdMap: Map<TestResultStatus, Int>,
        zephyrDataSetSteps: List<DetailedReportTestScriptResult>, testNgDataSetResult: TestNgDataSetResult
    ): MergeResult
}