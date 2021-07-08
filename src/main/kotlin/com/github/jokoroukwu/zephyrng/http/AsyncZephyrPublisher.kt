package com.github.jokoroukwu.zephyrng.http

import com.github.jokoroukwu.zephyrng.http.createtestcycle.CreateTestCycleRequestSender
import com.github.jokoroukwu.zephyrng.http.detailedreport.GetDetailedReportSender
import com.github.jokoroukwu.zephyrng.http.testcycleupdate.UpdateTestCycleRequestSender
import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.github.jokoroukwu.zephyrng.http.testresultupdate.TestResultUpdater
import com.github.jokoroukwu.zephyrng.http.testscriptresult.UpdateTestScriptResultsRequestSender
import com.github.jokoroukwu.zephyrng.updatableresult.UpdatableResultsMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.time.Duration
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class AsyncZephyrPublisher(
    private val createTestRunRequestSender: CreateTestCycleRequestSender = CreateTestCycleRequestSender(),
    private val updateTestCycleRequestSender: UpdateTestCycleRequestSender = UpdateTestCycleRequestSender(),
    private val updatableResultsMapper: UpdatableResultsMapper = UpdatableResultsMapper(),
    private val updateTestResultRequestSender: TestResultUpdater = TestResultUpdater(),
    private val updateTestScriptResultsSender: UpdateTestScriptResultsRequestSender = UpdateTestScriptResultsRequestSender(),
    private val getDetailedReportSender: GetDetailedReportSender = GetDetailedReportSender(),
) {

    /**
     * Performs a chain of actions to publish TestNG results.
     * <p>
     * Comprises execution of multiple HTTP-requests to create a new cycle,
     * populate it with test cases and finally update test results,
     * as well as intermediate operations like merging Zephyr data objects
     * with provided TestNG data.
     * Each [TestNgZephyrSuite] is processed asynchronously.
     *
     * @param projectId JIRA project id
     * @param testNgZephyrSuites TestNG test results partly merged with Zephyr data
     * @param testResultStatusToIdMap a map where every each Zephyr status is mapped to the corresponding id
     */
    fun submitResults(
        projectId: Long,
        testNgZephyrSuites: Collection<TestNgZephyrSuite>,
        testResultStatusToIdMap: Map<TestResultStatus, Long>
    ) {
        if (testNgZephyrSuites.isNotEmpty()) {
            logger.info { "Publishing TestNG test results" }
            measureTimeMillis {
                runBlocking {
                    launch(SupervisorJob() + Dispatchers.IO) {
                        for (suite in testNgZephyrSuites) {
                            val suiteName = suite.name
                            launch {
                                logger.debug { "Creating test cycle: {name: $suiteName}" }
                                val createTestCycleResponse =
                                    createTestRunRequestSender.createTestCycle(projectId, suite)
                                val testCycleKey = createTestCycleResponse.key
                                logger.debug {
                                    "test cycle successfully created: ${
                                        cycleInfo(
                                            suiteName,
                                            testCycleKey
                                        )
                                    }"
                                }

                                logger.debug {
                                    "Adding test cases: test_cycle: ${cycleInfo(suiteName, testCycleKey)}, " +
                                            "test_cases: ${
                                                suite.testCasesWithDataSetResults.map(
                                                    com.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults::key
                                                )
                                            }"
                                }
                                updateTestCycleRequestSender.putTestCasesRequest(suite, createTestCycleResponse)
                                logger.debug {
                                    "Test cases successfully added: test_cycle: ${
                                        cycleInfo(
                                            suiteName,
                                            testCycleKey
                                        )
                                    }"
                                }

                                logger.debug {
                                    "Fetching detailed report: test_cycle: ${
                                        cycleInfo(
                                            suiteName,
                                            testCycleKey
                                        )
                                    }"
                                }
                                val getDetailedResponse = getDetailedReportSender.getDetailedReport(
                                    projectId,
                                    testCycleKey,
                                )
                                logger.debug {
                                    "Detailed report successfully fetched: test_cycle: ${
                                        cycleInfo(
                                            suiteName,
                                            testCycleKey
                                        )
                                    }"
                                }

                                val updatablePair = updatableResultsMapper.mapToUpdatableResults(
                                    testCycleKey,
                                    getDetailedResponse.testRunsDetailReports.first().testResults,
                                    suite,
                                    testResultStatusToIdMap
                                )

                                logger.debug {
                                    "Updating step results: test_cycle: ${
                                        cycleInfo(
                                            suiteName,
                                            testCycleKey
                                        )
                                    }"
                                }
                                updateTestScriptResultsSender.updateTestScriptResults(testCycleKey, updatablePair.first)
                                logger.debug {
                                    "Step results successfully updated: test_cycle: ${
                                        cycleInfo(
                                            suiteName,
                                            testCycleKey
                                        )
                                    }"
                                }

                                logger.debug {
                                    "Updating test results: test_cycle: ${
                                        cycleInfo(
                                            suiteName,
                                            testCycleKey
                                        )
                                    }"
                                }
                                updateTestResultRequestSender.updateTestResult(
                                    testCycleKey,
                                    testResultStatusToIdMap,
                                    updatablePair.second
                                )
                                logger.debug {
                                    "Test results successfully updated: test_cycle: ${
                                        cycleInfo(
                                            suiteName,
                                            testCycleKey
                                        )
                                    }"
                                }
                                logger.debug {
                                    "TestNG test results successfully published: test_cycle: ${
                                        cycleInfo(
                                            suiteName,
                                            testCycleKey
                                        )
                                    }"
                                }
                            }
                        }
                    }.join()
                }
            }.logDuration()
        }
    }

    private fun Long.logDuration() {
        with(Duration.ofMillis(this)) {
            logger.info {
                "TestNG results published in ${seconds}.${toMillisPart()} seconds"
            }
        }
    }

    private fun cycleInfo(name: String, key: String) = "{name: '$name', key: '$key'}"
}


