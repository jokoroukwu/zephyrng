package com.gmail.johnokoroukwu.zephyrng.tests.endtoend

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.util.encodeBase64ToString
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.BasicCredentials
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import com.gmail.johnokoroukwu.zephyrng.ZephyrNgListener
import com.gmail.johnokoroukwu.zephyrng.config.IZephyrNgConfig
import com.gmail.johnokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.gmail.johnokoroukwu.zephyrng.datasetindexprovider.DataSetIndexProviderImpl
import com.gmail.johnokoroukwu.zephyrng.http.AbstractRequestSender
import com.gmail.johnokoroukwu.zephyrng.http.createtestcycle.CreateTestCycleRequest
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.TestCase
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.TestRunDetailReport
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.ZephyrTestResult
import com.gmail.johnokoroukwu.zephyrng.http.testcycleupdate.UpdateTestCycleRequest
import com.gmail.johnokoroukwu.zephyrng.instantformatter.InstantToStringFormatterImpl
import com.gmail.johnokoroukwu.zephyrng.tests.endtoend.util.*
import com.gmail.johnokoroukwu.zephyrng.tests.endtoend.util.CustomRequestMatcher.urlEndsWith
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
    private val idOne = 1
    private val idTwo = 2
    private val suiteMock = mockk<ISuite>()
    private lateinit var passedDataDrivenTestResult: ITestResult
    private lateinit var failedDataDrivenTestResult: ITestResult
    private lateinit var passedNonDataDrivenTestResult: ITestResult


    private val dataSetIndexProvider = DataSetIndexProviderImpl()
    private val wireMock = WireMockServer(WireMockConfiguration().httpsPort(8089))


    private lateinit var getTestCasesStub: UUID
    private lateinit var getTestResultStatusesStub: UUID
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
        failedDataDrivenTestResult = mockk {
            every { testClass.realClass } returns DummyTest::class.java
            every { method } returns dataDrivenMethod
            every { parameters } returns dataSetOne
            every { startMillis } returns System.currentTimeMillis()
            every { endMillis } returns System.currentTimeMillis() + 10
            every { isSuccess } returns false
            every { throwable } returns RuntimeException("data driven method exception").apply {
                stackTrace = arrayOf(
                    StackTraceElement(
                        DummyTest::class.java.name,
                        "someStep",
                        null,
                        -1
                    )
                )
            }
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
        passedNonDataDrivenTestResult = mockk {
            every { testClass.realClass } returns DummyTest::class.java
            every { startMillis } returns System.currentTimeMillis()
            every { endMillis } returns System.currentTimeMillis() + 10
            every { method } returns nonDataDrivenMethod
            every { isSuccess } returns true
            every { throwable } returns null
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
            getDetailedReportStub = stubGetDetailedReportRequest(
                TestRunDetailReport(
                    listOf(
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
                )
            )
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
        wireMock.requestsForStub(createTestCycleStub).also {
            Assertions.assertThat(it)
                .`as`("outbound create test cycle requests count validation")
                .hasSize(1)
            with(SoftAssertions()) {
                val request = it.first()
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

    }

    @Test
    fun `should submit single valid 'update test cycle request'`() {
        val request = wireMock.requestsForStub(updateTestCycleStub).also {
            Assertions.assertThat(it)
                .`as`("outbound update test cycle request count validation")
                .hasSize(1)
        }.first()

        with(SoftAssertions()) {
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

    @AfterClass(alwaysRun = true)
    fun tearDown() {
        unmockkAll()
        wireMock.stop()
    }


    private fun WireMockServer.requestsForStub(id: UUID) = allServeEvents.stream()
        .filter { event -> event.stubMapping.id == id }
        .map(ServeEvent::getRequest)
        .collect(Collectors.toList())

    private fun IZephyrNgConfig.basicAuthBase64() = "Basic ${"${username()}:${password()}".encodeBase64ToString()}"

}