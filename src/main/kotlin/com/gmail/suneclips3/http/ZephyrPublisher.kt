package com.gmail.suneclips3.http

import com.gmail.suneclips3.TestCaseWithTestNgResults
import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.http.createtestcycle.CreateTestCycleRequestSender
import com.gmail.suneclips3.http.detailedreport.GetDetailedReportSender
import com.gmail.suneclips3.http.testcycleupdate.UpdateTestCycleRequestSender
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus
import com.gmail.suneclips3.http.testresultupdate.UpdateTestResultRequestSender
import com.gmail.suneclips3.http.testscriptresult.UpdateTestScriptResultsRequestSender
import com.gmail.suneclips3.updatableresultsmapper.UpdatableResultsMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ZephyrPublisher(
    private val zephyrConfig: ZephyrConfig,
    private val createTestRunRequestSender: CreateTestCycleRequestSender = CreateTestCycleRequestSender(zephyrConfig),
    private val updateTestCycleRequestSender: UpdateTestCycleRequestSender = UpdateTestCycleRequestSender(
        zephyrConfig
    ),
    private val updatableResultsMapper: UpdatableResultsMapper = UpdatableResultsMapper(),
    private val updateTestResultRequestSender: UpdateTestResultRequestSender = UpdateTestResultRequestSender(
        zephyrConfig
    ),
    private val updateTestScriptResultsSender: UpdateTestScriptResultsRequestSender = UpdateTestScriptResultsRequestSender(
        zephyrConfig
    ),
    private val getDetailedReportSender: GetDetailedReportSender = GetDetailedReportSender(zephyrConfig),

    ) {

    fun submitResults(
        projectId: Int,
        suitesWithTestCaseResults: Collection<TestSuiteWithTestCaseResults>,
        testResultStatusToIdMap: Map<TestResultStatus, Int>
    ) {
        runBlocking {

            launch(SupervisorJob() + Dispatchers.IO) {
                for (suite in suitesWithTestCaseResults) {
                    launch {
                        println("creating Zephyr test cycle: {name: ${suite.name}}")
                        val createTestCycleResponse = createTestRunRequestSender.createTestCycle(
                            projectId,
                            suite,
                            zephyrConfig
                        )
                        val testCycleKey = createTestCycleResponse.key
                        println("test cycle created: {name: ${suite.name}, key: $testCycleKey}")

                        println(
                            "adding cases to created test cycle: {test_cycle_name: ${suite.name}," +
                                    " test_cycle_key: $testCycleKey}," +
                                    " test_cases_keys: ${suite.testCasesWithDataSetResults.map(TestCaseWithTestNgResults::id)}"
                        )
                        updateTestCycleRequestSender.putTestCasesRequest(
                            suite = suite,
                            createTestCycleResponse = createTestCycleResponse,
                            zephyrConfig = zephyrConfig
                        )

                        println("fetching detailed report: {test_cycle_key: $testCycleKey}")
                        val getDetailedResponse = getDetailedReportSender.getDetailedReport(
                            projectId,
                            testCycleKey,
                            zephyrConfig
                        )
                        val updatablePair = updatableResultsMapper.mapToUpdatableResults(
                            getDetailedResponse,
                            testResultStatusToIdMap,
                            suite
                        )

                        updateTestScriptResultsSender.updateTestScriptResults(testCycleKey, updatablePair.first)
                        updateTestResultRequestSender.updateTestResult(
                            testCycleKey,
                            testResultStatusToIdMap,
                            updatablePair.second
                        )
                    }
                }
            }.join()
        }
    }
}


