package com.gmail.suneclips3.updatableresultsmapper

import com.gmail.suneclips3.CommentRow
import com.gmail.suneclips3.UpdatableTestResult
import com.gmail.suneclips3.http.TestSuiteWithTestCaseResults
import com.gmail.suneclips3.http.detailedreport.GetDetailedReportResponse
import com.gmail.suneclips3.http.detailedreport.TestScriptResult
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus
import com.gmail.suneclips3.updatableresultsmapper.merge.FailedResultMergeStrategy
import com.gmail.suneclips3.updatableresultsmapper.merge.IResultMergeStrategy
import com.gmail.suneclips3.updatableresultsmapper.merge.MergeStrategyDelegator
import com.gmail.suneclips3.updatableresultsmapper.merge.PassedResultMergeStrategy

class UpdatableResultsMapper(
    private val zephyrTestResultToDataSetMapper: IZephyrTestResultToDataSetMapper = ZephyrResultToDataSetMapper(),
    private val mergeStrategy: IResultMergeStrategy = MergeStrategyDelegator(
        HashMap<Boolean, IResultMergeStrategy>().apply {
            put(true, PassedResultMergeStrategy)
            put(false, FailedResultMergeStrategy)
        })

) {

    fun mapToUpdatableResults(
        getDetailedReportResponse: GetDetailedReportResponse,
        testResultStatusToIdMap: Map<TestResultStatus, Int>,
        suiteWithTestCaseResults: TestSuiteWithTestCaseResults
    ): Pair<List<TestScriptResult>, List<UpdatableTestResult>> {

        return toUpdatableTestResultsAndScriptResults(
            getDetailedReportResponse,
            suiteWithTestCaseResults,
            testResultStatusToIdMap
        )
    }


    private fun toUpdatableTestResultsAndScriptResults(
        getDetailedReportResponse: GetDetailedReportResponse,
        testSuite: TestSuiteWithTestCaseResults,
        testResultStatusToIdMap: Map<TestResultStatus, Int>
    ): Pair<List<TestScriptResult>, List<UpdatableTestResult>> {
        val testCycleKey = getDetailedReportResponse.testRunsDetailReports.first().testRun.key
        val idToZephyrTestResult = getDetailedReportResponse.testRunsDetailReports
            .first().testResults
            .associateBy { it.testCase.id }

        val updatableTestResults = ArrayList<UpdatableTestResult>()
        val updatableTestScriptResults = ArrayList<TestScriptResult>(getDetailedReportResponse.testRunsDetailReports
            .first().testResults
            .sumOf { it.testScriptResults.size }
        )

        for (testCaseWithTestNgResult in testSuite.testCasesWithDataSetResults) {
            val zephyrTestResult = idToZephyrTestResult[testCaseWithTestNgResult.id]
            if (zephyrTestResult == null) {
                //  this may only happen if result was deleted somewhere between its creation and update
                "ERROR result will not be updated: {reason: no mapping for given test case id, " +
                        "test_case: {key: ${testCaseWithTestNgResult.key}, id: ${testCaseWithTestNgResult.id}}}"
            } else {
                val zephyrDataSets = zephyrTestResultToDataSetMapper.mapTestResultToZephyrDataSets(zephyrTestResult)
                val testNgDataSets = testCaseWithTestNgResult.dataSetResults
                val commentRows = ArrayList<CommentRow>(5)

                testNgDataSets.forEachIndexed { i, testNgDataSet ->
                    zephyrDataSets.getOrElse(i) { emptyList() }.also { zephyrDataSet ->
                        mergeStrategy.mergeResults(testResultStatusToIdMap, zephyrDataSet, testNgDataSet).apply {
                            error?.also {
                                println(
                                    "WARNING $it: {test_cycle_key: $testCycleKey, test_case_key: ${testCaseWithTestNgResult.key}}"
                                )
                            }
                            commentRow?.apply(commentRows::add)
                            updatableTestScriptResults.addAll(testScriptResults)
                        }
                    }
                }
                updatableTestResults.add(
                    UpdatableTestResult(
                        startTime = testSuite.startTime,
                        endTime = testSuite.endTime,
                        testResultId = zephyrTestResult.id,
                        commentRows = commentRows,
                    )
                )
            }
        }
        return updatableTestScriptResults to updatableTestResults
    }
}


