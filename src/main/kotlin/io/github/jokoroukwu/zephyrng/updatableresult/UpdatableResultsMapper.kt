package io.github.jokoroukwu.zephyrng.updatableresult

import io.github.jokoroukwu.zephyrng.http.TestNgZephyrSuite
import io.github.jokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import io.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrTestResult
import io.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import io.github.jokoroukwu.zephyrng.updatableresult.merge.MergeStrategyDelegator
import io.github.jokoroukwu.zephyrng.updatableresult.merge.ResultMergeStrategy
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger { }

class UpdatableResultsMapper(
    private val zephyrTestResultToDataSetMapper: ZephyrTestResultToDataSetMapper = ZephyrResultToDataSetMapperImpl,
    private val mergeStrategy: ResultMergeStrategy = MergeStrategyDelegator()
) {

    fun mapToUpdatableResults(
        testCycleKey: String,
        detailedReportResultZephyrs: List<ZephyrTestResult>,
        suiteWithResults: TestNgZephyrSuite,
        testResultStatusToIdMap: Map<TestResultStatus, Long>
    ): Pair<List<TestScriptResult>, List<UpdatableTestResult>> {

        return toUpdatableTestResultsAndScriptResults(
            testCycleKey,
            detailedReportResultZephyrs,
            suiteWithResults,
            testResultStatusToIdMap
        )
    }


    private fun toUpdatableTestResultsAndScriptResults(
        testCycleKey: String,
        detailedReportResultZephyrs: List<ZephyrTestResult>,
        testSuite: TestNgZephyrSuite,
        testResultStatusToIdMap: Map<TestResultStatus, Long>
    ): Pair<List<TestScriptResult>, List<UpdatableTestResult>> {
        val idToZephyrTestResult = detailedReportResultZephyrs.let {
            it.associateByTo(HashMap(it.size, 1F)) { result -> result.testCase.id }
        }

        val updatableTestResults = ArrayList<UpdatableTestResult>()
        val updatableTestScriptResults =
            ArrayList<TestScriptResult>(detailedReportResultZephyrs.sumOf { it.testScriptResults.size })

        for (testCaseWithTestNgResult in testSuite.testCasesWithDataSetResults) {
            val zephyrTestResult = idToZephyrTestResult[testCaseWithTestNgResult.id]
            if (zephyrTestResult == null) {
                logZephyrResultDeletedError(testCaseWithTestNgResult.key)
            } else {
                val zephyrDataSets = zephyrTestResultToDataSetMapper.mapTestResultToZephyrDataSets(zephyrTestResult)
                val testNgDataSets = testCaseWithTestNgResult.dataSetResults
                val commentRows = TreeSet<CommentRow>()
                val effectiveStatusSet = EnumSet.noneOf(TestResultStatus::class.java)
                testNgDataSets.forEachIndexed { i, testNgDataSet ->
                    zephyrDataSets.getOrElse(i) { emptyList() }.also { zephyrDataSet ->
                        mergeStrategy.mergeResults(testResultStatusToIdMap, zephyrDataSet, testNgDataSet).apply {
                            error?.also { logMergeWarning(error, testCycleKey, testCaseWithTestNgResult.key) }
                            commentRow?.apply(commentRows::add)
                            updatableTestScriptResults.addAll(testScriptResults)
                            effectiveStatusSet.add(status)
                        }
                    }
                }
                updatableTestResults.add(
                    UpdatableTestResult(
                        startTime = testCaseWithTestNgResult.startTime,
                        endTime = testCaseWithTestNgResult.endTime,
                        testResultId = zephyrTestResult.id,
                        commentRows = commentRows,
                        effectiveStatus = effectiveStatusSet.getEffectiveStatus()
                    )
                )
            }
        }
        return updatableTestScriptResults to updatableTestResults
    }

    private fun logMergeWarning(error: String, testCycleKey: String, testCaseKey: String) {
        logger.warn { "$error: {test_cycle_key: $testCycleKey, test_case_key: $testCaseKey}" }
    }

    private fun logZephyrResultDeletedError(testCaseKey: String) {
        logger.error {
            "Result will not be updated: {test_case_key: $testCaseKey, " +
                    "reason: no mapping for given test case " +
                    "(this may only happen if test result had been removed from JIRA server after creation but before it was updated)}"
        }
    }

    private fun Set<TestResultStatus>.getEffectiveStatus() =
        if (contains(TestResultStatus.FAIL)) TestResultStatus.FAIL else TestResultStatus.PASS
}


