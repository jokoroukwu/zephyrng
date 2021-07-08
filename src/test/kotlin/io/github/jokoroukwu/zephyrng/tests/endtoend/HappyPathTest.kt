package io.github.jokoroukwu.zephyrng.tests.endtoend

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.util.encodeBase64ToString
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.BasicCredentials
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import io.github.jokoroukwu.zephyrng.ZephyrNgListener
import io.github.jokoroukwu.zephyrng.config.IZephyrNgConfig
import io.github.jokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import io.github.jokoroukwu.zephyrng.datasetindexprovider.DataSetIndexProviderImpl
import io.github.jokoroukwu.zephyrng.http.AbstractRequestSender
import io.github.jokoroukwu.zephyrng.http.JsonMapper
import io.github.jokoroukwu.zephyrng.http.createtestcycle.CreateTestCycleRequest
import io.github.jokoroukwu.zephyrng.http.detailedreport.*
import io.github.jokoroukwu.zephyrng.http.testcycleupdate.UpdateTestCycleRequest
import io.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import io.github.jokoroukwu.zephyrng.instantformatter.InstantToStringFormatterImpl
import io.github.jokoroukwu.zephyrng.tests.endtoend.util.*
import io.github.jokoroukwu.zephyrng.tests.endtoend.util.CustomRequestMatcher.urlEndsWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.testng.IResultMap
import org.testng.ISuite
import org.testng.ITestNGMethod
import org.testng.ITestResult
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.testng.internal.ConstructorOrMethod
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

class HappyPathTest {
    private val zephyrNgConfig = ZephyrNgConfigLoaderImpl.zephyrNgConfig()
    private val instantFormatter = InstantToStringFormatterImpl()
    private val idOne = 1L
    private val idTwo = 2L
    private val stubbedException = RuntimeException("data driven method exception")
    private val suiteMock = mockk<ISuite>()
    private lateinit var passedDataDrivenTestResult: ITestResult
    private lateinit var failedDataDrivenTestResult: ITestResult
    private lateinit var passedNonDataDrivenTestResult: ITestResult


    private val dataSetIndexProvider = DataSetIndexProviderImpl()
    private val wireMock = WireMockServer(WireMockConfiguration().httpsPort(8089))
    private val stubbedZephyrTestResults = listOf(
        ZephyrTestResult(
            id = idOne,
            testCase = TestCase(
                id = idOne,
                key = DATA_DRIVEN_TEST_CASE_KEY,
                name = DATA_DRIVEN_TEST_CASE_KEY
            ),
            testScriptResults = listOf(
                ZephyrStepResult(id = 1, index = 0),
                ZephyrStepResult(id = 2, index = 1),
                ZephyrStepResult(id = 3, index = 2),
                ZephyrStepResult(id = 4, index = 0),
                ZephyrStepResult(id = 5, index = 1),
                ZephyrStepResult(id = 6, index = 2)
            )
        ),
        ZephyrTestResult(
            id = idTwo,
            testCase = TestCase(
                id = idTwo,
                key = NON_DATA_DRIVEN_TEST_CASE,
                name = NON_DATA_DRIVEN_TEST_CASE
            ),
            testScriptResults = listOf(
                ZephyrStepResult(id = 7, index = 0),
                ZephyrStepResult(id = 8, index = 1),
                ZephyrStepResult(id = 9, index = 2)
            )
        )
    )

    private lateinit var createTestCycleStub: UUID
    private lateinit var updateTestCycleStub: UUID
    private lateinit var updateTestResultsStub: UUID
    private lateinit var updateTestScriptResultsStub: UUID
    private lateinit var getDetailedReportStub: UUID


    @BeforeClass
    fun setUp() {
        every { suiteMock.name } returns "test-suite"
        val dataSetOne = arrayOf<Any?>("arg1", "arg2")
        val dataSetTwo = arrayOf<Any?>("arg3", "arg4")

        val dataDrivenMethod = mockk<ITestNGMethod> {
            every { constructorOrMethod } returns ConstructorOrMethod(
                DummyTest::class.java.getMethod("data driven method")
            )
            every { isDataDriven } returns true
        }
        val nonDataDrivenMethod = mockk<ITestNGMethod> {
            every { constructorOrMethod } returns ConstructorOrMethod(
                DummyTest::class.java.getMethod("non data driven method")
            )
            every { isDataDriven } returns false
        }
        passedNonDataDrivenTestResult = mockk {
            every { testClass.realClass } returns DummyTest::class.java
            every { startMillis } returns System.currentTimeMillis()
            every { endMillis } returns System.currentTimeMillis() + 10
            every { method } returns nonDataDrivenMethod
            every { isSuccess } returns true
            every { throwable } returns null
        }
        passedDataDrivenTestResult = mockk {
            every { testClass.realClass } returns DummyTest::class.java
            every { startMillis } returns System.currentTimeMillis()
            every { endMillis } returns System.currentTimeMillis() + 10
            every { method } returns dataDrivenMethod
            every { parameters } returns dataSetTwo
            every { isSuccess } returns true
            every { throwable } returns null
        }

        failedDataDrivenTestResult = mockk {
            every { testClass.realClass } returns DummyTest::class.java
            every { method } returns dataDrivenMethod
            every { parameters } returns dataSetOne
            every { startMillis } returns System.currentTimeMillis()
            every { endMillis } returns System.currentTimeMillis() + 10
            every { isSuccess } returns false
            every { throwable } returns stubbedException.apply {
                stackTrace = arrayOf(
                    StackTraceElement(DummyTest::class.java.name, "someStep", null, -1)
                )
            }
        }


        val emptyResultMap = mockk<IResultMap> {
            every { allResults } returns emptySet()
        }
        every { suiteMock.results } returns hashMapOf(
            "1" to mockk {
                every { testContext.skippedTests } returns emptyResultMap
                every { testContext.passedTests.allResults } returns hashSetOf(
                    passedDataDrivenTestResult,
                    passedNonDataDrivenTestResult
                )
                every { testContext.failedTests.allResults } returns hashSetOf(
                    failedDataDrivenTestResult
                )
            }
        )
        val iterator = arrayOf(dataSetOne, dataSetTwo).toMutableList().iterator()
        with(wireMock) {
            start()
            stubGetTestCasesRequest()
            stubGetTestResultStatusesRequest()
            createTestCycleStub = stubCreateTestCycleRequest()
            updateTestCycleStub = stubUpdateTestCycleRequest()
            getDetailedReportStub = stubGetDetailedReportRequest(TestRunDetailReport(stubbedZephyrTestResults))
            updateTestResultsStub = stubUpdateTestResultsRequest()
            updateTestScriptResultsStub = stubUpdateTestScriptResultsRequest()
            dataSetIndexProvider.intercept(iterator, null, dataDrivenMethod, null)

            ZephyrNgListener(dataSetIndexProvider = dataSetIndexProvider)
                .generateReport(null, mutableListOf(suiteMock), null)
        }
    }

    @Test
    fun `should submit single valid getTestCases request`() {
        val inClause = listOf(DATA_DRIVEN_TEST_CASE_KEY, NON_DATA_DRIVEN_TEST_CASE).joinToString(
            prefix = "('", separator = "','", postfix = "')"
        )
        verify(
            exactly(1), getRequestedFor(anyUrl())
                .withBasicAuth(BasicCredentials(zephyrNgConfig.username(), zephyrNgConfig.password()))
                .andMatching(urlEndsWith(inClause))
        )
    }

    @Test
    fun `should submit single valid getTestResultStatuses request`() {
        with("${AbstractRequestSender.BASE_API_URL}/project/$DEFAULT_PROJECT_ID/testresultstatus") {
            verify(
                exactly(1), getRequestedFor(urlEqualTo(this))
                    .withBasicAuth(BasicCredentials(zephyrNgConfig.username(), zephyrNgConfig.password()))
            )
        }
    }

    @Test
    fun `should submit single valid createTestCycleRequest`() {
        val request = wireMock.assertHasSingleRequest(
            createTestCycleStub, "CreateTestCycle requests count validation"
        )
        softly {
            assertThat(request.header(Headers.AUTHORIZATION).firstValue())
                .`as`("")
                .isEqualTo(zephyrNgConfig.basicAuthBase64())
            assertThat(request.bodyAsString)
                .`as`("request body validation")
                .satisfies { body ->
                    var createTestCycleRequest: CreateTestCycleRequest? = null
                    Assertions.assertThatCode {
                        Json.decodeFromString<CreateTestCycleRequest>(body).also { pojo ->
                            createTestCycleRequest = pojo
                        }
                    }.`as`("body deserialization")
                        .doesNotThrowAnyException()

                    val description = "CreateTestCycleRequest: %s"
                    val results = listOf(
                        failedDataDrivenTestResult,
                        passedDataDrivenTestResult,
                        passedNonDataDrivenTestResult
                    )
                    val minTime = results.minOf { result -> result.startMillis }
                        .run(Instant::ofEpochMilli)
                        .run(instantFormatter::formatInstant)
                    val maxTime = results.maxOf { result -> result.endMillis }
                        .run(Instant::ofEpochMilli)
                        .run(instantFormatter::formatInstant)

                    assertThat(createTestCycleRequest!!.name)
                        .`as`(description, "name")
                        .isEqualTo("${suiteMock.name} {time: $minTime - $maxTime}")

                    assertThat(createTestCycleRequest!!.plannedStartDate)
                        .`as`(description, "planned start date")
                        .isEqualTo(minTime)

                    assertThat(createTestCycleRequest!!.plannedEndDate)
                        .`as`(description, "planned start date")
                        .isEqualTo(maxTime)

                    assertThat(createTestCycleRequest!!.projectId)
                        .`as`(description, "project id")
                        .isEqualTo(DEFAULT_PROJECT_ID)
                }
            assertAll()
        }
    }

    @Test
    fun `should submit single valid 'update test cycle request'`() {
        val request = wireMock.assertHasSingleRequest(
            updateTestCycleStub, "UpdateTestCycle request count validation"
        )

        softly {
            val description = "Update test cycle request: %s"
            assertThat(request.header(Headers.AUTHORIZATION).firstValue())
                .`as`(description, "basic authorization header")
                .isEqualTo(zephyrNgConfig.basicAuthBase64())

            assertThat(request.method)
                .`as`(description, "method")
                .isEqualTo(RequestMethod.PUT)

            assertThat(request)
                .`as`(description, "body")
                .satisfies { rq ->
                    val updateTestCycleRequest = Json.decodeFromString<UpdateTestCycleRequest>(rq.bodyAsString)
                    assertThat(updateTestCycleRequest.testRunId)
                        .`as`(description, "test run id")
                        .isEqualTo(DEFAULT_CYCLE_ID)

                    assertThat(updateTestCycleRequest.addedTestRunItems.map { it.lastTestResult.testCaseId })
                        .`as`(description, "test case ids")
                        .containsExactlyInAnyOrder(1, 2)

                    assertThat(updateTestCycleRequest.addedTestRunItems.map { it.index })
                        .`as`(description, "added test run items' indexes")
                        .containsExactlyInAnyOrder(0, 1)

                    assertThat(updateTestCycleRequest.addedTestRunItems.map { it.id })
                        .`as`(description, "added test run items' ids")
                        .containsOnlyNulls()
                }
            assertAll()
        }
    }

    @Test
    fun `should submit single valid getDetailedReportRequest`() {
        val request = wireMock.assertHasSingleRequest(
            getDetailedReportStub, "get detailed report requests count validation"
        )

        softly {
            val description = "GetDetailedReport request: %s"
            assertThat(request.header(Headers.AUTHORIZATION).firstValue())
                .`as`(description, "basic authorization header")
                .isEqualTo(zephyrNgConfig.basicAuthBase64())

            assertThat(request.method)
                .`as`(description, "method")
                .isEqualTo(RequestMethod.GET)

        }

    }

    @Test
    fun `should submit single valid updateTestResultsRequest`() {
        val request = wireMock.assertHasSingleRequest(
            updateTestResultsStub, "UpdateTestResult requests count validation"
        )

        softly {
            val description = "UpdateTestResultsRequest request: %s"
            assertThat(request.header(Headers.AUTHORIZATION).firstValue())
                .`as`(description, "basic authorization header")
                .isEqualTo(zephyrNgConfig.basicAuthBase64())

            assertThat(request.method)
                .`as`(description, "method")
                .isEqualTo(RequestMethod.PUT)

            assertThat(request.bodyAsString)
                .satisfies { body ->
                    val testResults = JsonMapper.instance.decodeFromString<List<TestScriptResult>>(body)
                    val expectedResultOne = testResults.find { it.id == idOne }
                    val expectedResultTwo = testResults.find { it.id == idTwo }

                    Assertions.assertThat(expectedResultOne)
                        .`as`("should contain expected result one")
                        .isNotNull

                    Assertions.assertThat(expectedResultTwo)
                        .`as`("should contain expected result two")
                        .isNotNull

                    assertThat(expectedResultOne)
                        .`as`("result one should have FAIL status")
                        .extracting { it!!.testResultStatusId }
                        .isEqualTo(TestResultStatus.FAIL.ordinal.toLong())

                    assertThat(expectedResultTwo)
                        .`as`("result two should have PASS status")
                        .extracting { it!!.testResultStatusId }
                        .isEqualTo(TestResultStatus.PASS.ordinal.toLong())

                    assertThat(expectedResultOne)
                        .`as`("result one should have empty comment")
                        .extracting { it!!.comment }
                        .asString()
                        .isEmpty()

                    assertThat(expectedResultTwo)
                        .`as`("result two should have empty comment")
                        .extracting { it!!.comment }
                        .asString()
                        .isEmpty()
                }
            assertAll()
        }
    }

    @Test
    fun `should submit single valid updateTestScriptResultsRequest`() {
        val request = wireMock.assertHasSingleRequest(
            updateTestScriptResultsStub, "UpdateTestScriptResultsRequest count validation"
        )

        softly {
            val description = "UpdateTestScriptResultsRequest request: %s"
            assertThat(request.header(Headers.AUTHORIZATION).firstValue())
                .`as`(description, "basic authorization header")
                .isEqualTo(zephyrNgConfig.basicAuthBase64())

            assertThat(request.method)
                .`as`(description, "method")
                .isEqualTo(RequestMethod.PUT)

            assertThat(request.bodyAsString)
                .satisfies { body ->
                    val actualTestScriptResults = JsonMapper.instance.decodeFromString<List<TestScriptResult>>(body)
                    val dataSetOneScriptResults = stubbedZephyrTestResults[0].testScriptResults

                    val expectedPassedResults = dataSetOneScriptResults.subList(0, 4)
                        .plus(stubbedZephyrTestResults[1].testScriptResults).mapTo(ArrayList()) {
                            TestScriptResult(id = it.id, testResultStatusId = TestResultStatus.PASS.ordinal.toLong())
                        }
                    val expectedFailedTestScriptResult = TestScriptResult(
                        id = dataSetOneScriptResults[4].id,
                        testResultStatusId = TestResultStatus.FAIL.ordinal.toLong(),
                        comment = stubbedException.toString()
                    )
                    val expectedBlockedTestScriptResult = TestScriptResult(
                        id = dataSetOneScriptResults[5].id,
                        testResultStatusId = TestResultStatus.BLOCKED.ordinal.toLong()
                    )

                    assertThat(actualTestScriptResults)
                        .`as`("should contain expected test script results ")
                        .containsExactlyInAnyOrderElementsOf(
                            expectedPassedResults.apply {
                                add(expectedFailedTestScriptResult)
                                add(expectedBlockedTestScriptResult)
                            }
                        )
                }
            assertAll()
        }
    }

    @AfterClass(alwaysRun = true)
    fun tearDown() {
        unmockkAll()
        wireMock.stop()
    }

    private fun WireMockServer.assertHasSingleRequest(id: UUID, description: String) = allServeEvents.stream()
        .filter { event -> event.stubMapping.id == id }
        .map(ServeEvent::getRequest)
        .collect(Collectors.toList())
        .also { Assertions.assertThat(it).`as`(description).hasSize(1) }[0]

    private inline fun <T> softly(assertion: SoftAssertions.() -> T) = assertion(SoftAssertions())


    private fun IZephyrNgConfig.basicAuthBase64() = "Basic ${"${username()}:${password()}".encodeBase64ToString()}"

}