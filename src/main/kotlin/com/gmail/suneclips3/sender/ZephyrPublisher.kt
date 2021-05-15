package com.gmail.suneclips3.sender

import com.gmail.suneclips3.ZephyrTestCycle
import com.gmail.suneclips3.ZephyrTestResult
import com.gmail.suneclips3.dto.ConnectionConfig
import com.gmail.suneclips3.dto.internal.test_run.update_test_run.SerializableTestRunItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ZephyrPublisher(
    private val connectionConfig: ConnectionConfig,
    private val createTestRunRequestSender: CreateTestCycleRequestSender = CreateTestCycleRequestSender,
    private val putTestCasesRequestSender: PutTestCasesRequestSender = PutTestCasesRequestSender,
    private val getTestRunDetailedReportSender: GetTestRunDetailedReportSender = GetTestRunDetailedReportSender
) {

    fun submitTestResults(zephyrTestCycles: Collection<ZephyrTestCycle>) {
        if (zephyrTestCycles.isNotEmpty()) {
            submitResultsAsync(zephyrTestCycles)
        } else {
            println("No test results to publish")
        }
    }

    private fun submitResultsAsync(zephyrTestCycles: Collection<ZephyrTestCycle>) = runBlocking {
        launch(SupervisorJob() + Dispatchers.IO) {
            for (testCycle in zephyrTestCycles) {
                launch {
                    println("creating Zephyr test cycle: {name: ${testCycle.name}}")
                    val createTestCycleResponse = createTestRunRequestSender.createTestCycle(
                        testCycle,
                        connectionConfig
                    )

                    println(
                        "adding cases to created test cycle: {test_cycle_name: ${testCycle.name}," +
                                " test_cycle_key: ${createTestCycleResponse.key}," +
                                " test_cases: ${testCycle.zephyrTestResults.map(ZephyrTestResult::testCaseId)}"
                    )
                    putTestCasesRequestSender.putTestCasesRequest(
                        zephyrTestCycle = testCycle,
                        createTestCycleResponse = createTestCycleResponse,
                        connectionConfig = connectionConfig
                    )
                    println("fetching detailed report: {test_cycle_key: ${createTestCycleResponse.key}}")
                    val getDetailedReportResponse = getTestRunDetailedReportSender.getDetailedReport(
                        testCycle.zephyrProjectId,
                        createTestCycleResponse.key,
                        connectionConfig
                    )


                }
            }
        }
    }

}


