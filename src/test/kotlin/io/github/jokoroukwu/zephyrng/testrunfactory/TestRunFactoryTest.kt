package io.github.jokoroukwu.zephyrng.testrunfactory

import io.github.jokoroukwu.zephyrapi.publication.TestDataResultBase
import io.github.jokoroukwu.zephyrng.TestRunFactory
import io.github.jokoroukwu.zephyrng.testdataresultfactory.TimestampedTestDataResult
import io.github.jokoroukwu.zephyrng.testdataresultfactory.TimestampedTestDataResultFactory
import io.mockk.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.InstanceOfAssertFactories
import org.assertj.core.api.SoftAssertions
import org.testng.ISuite
import org.testng.ITestResult
import org.testng.annotations.AfterClass
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.testng.internal.TestResult

class TestRunFactoryTest {
    companion object {
        private val timestampedTestDataResultFactory: TimestampedTestDataResultFactory = mockk(relaxed = true)
        private val suiteMock: ISuite = mockk(relaxed = true)
    }

    private lateinit var testRunFactory: TestRunFactory

    @BeforeMethod(alwaysRun = true)
    fun setUp() {
        testRunFactory = TestRunFactory(timestampedTestDataResultFactory)
    }

    @AfterMethod(alwaysRun = true)
    fun clearMocks() {
        clearMocks(timestampedTestDataResultFactory, suiteMock)
    }

    @AfterClass(alwaysRun = true)
    fun unMock() {
        unmockkAll()
    }

    @Test
    fun `should ignore empty suite`() {
        every { suiteMock.results } returns emptyMap()

        val result = testRunFactory.getTestRuns(listOf(suiteMock))
        Assertions.assertThat(result)
            .`as`("null suite validation")
            .isEmpty()
    }

    @Test
    fun `test case should have expected start and end times`() {
        val minStartTime = 1L
        val maxEndTime = 10L
        val testCaseKey = "test-case-key"

        val testResult = TestResult.newEmptyTestResult().apply { testName = "test-one" }
        val testResult2 = TestResult.newEmptyTestResult().apply { testName = "test-two" }

        val testDataResult = TimestampedTestDataResult(minStartTime, maxEndTime, TestDataResultBase(0, true))
        val testDataResult2 = TimestampedTestDataResult(minStartTime + 1, maxEndTime, TestDataResultBase(1, true))

        every { timestampedTestDataResultFactory.getTestNgTestDataResult(any()) } returnsMany listOf(
            Pair(testCaseKey, testDataResult), Pair(testCaseKey, testDataResult2)
        )
        every { suiteMock.results } returns hashMapOf(
            "1" to mockk(relaxed = true) {
                every { testContext } returns mockk(relaxed = true) {
                    every { passedTests.allResults } returns setOf(testResult, testResult2)
                }
            }
        )
        val testRun = testRunFactory.getTestRuns(listOf(suiteMock)).first()
        Assertions.assertThat(testRun)
            .`as`("test cases with results")
            .extracting { it.testResults }
            .satisfies { testResults ->
                Assertions.assertThat(testResults)
                    .`as`("test case count")
                    .hasSize(1)
                Assertions.assertThat(testResults.first().testDataResults)
                    .`as`("data set results count")
                    .hasSize(2)
            }

        with(SoftAssertions())
        {
            assertThat(testRun.testResults.first().startTime)
                .`as`("start time validation")
                .isEqualTo(minStartTime)

            assertThat(testRun.testResults.first().endTime)
                .`as`("end time validation")
                .isEqualTo(maxEndTime)

            assertAll()
        }
    }

    @Test
    fun `test results should be grouped properly`() {
        val testResult = TestResult.newEmptyTestResult().apply { testName = "name-one" }
        val testResult2 = TestResult.newEmptyTestResult().apply { testName = "name-two" }
        val testResult3 = TestResult.newEmptyTestResult().apply { testName = "name-three" }

        every { suiteMock.results } returns hashMapOf(
            "1" to mockk(relaxed = true) {
                every { testContext } returns mockk(relaxed = true) {
                    every { passedTests.allResults } returns setOf(testResult)
                }
            },
            "2" to mockk(relaxed = true) {
                every { testContext } returns mockk(relaxed = true) {
                    every { passedTests.allResults } returns setOf(testResult2, testResult3)
                }
            }
        )

        val testCaseKeyOne = "key-one"
        val testCaseKeyTwo = "key-two"
        val dataSetResult = TimestampedTestDataResult(1, 2, TestDataResultBase(0, true))
        val dataSetResult2 = dataSetResult.copy(testDataResult = dataSetResult.testDataResult.copy(index = 1))
        val dataSetResult3 = dataSetResult.copy(testDataResult = dataSetResult.testDataResult.copy(index = 0))
        every { timestampedTestDataResultFactory.getTestNgTestDataResult(any()) } returnsMany listOf(
            Pair(testCaseKeyOne, dataSetResult),
            Pair(testCaseKeyOne, dataSetResult2),
            Pair(testCaseKeyTwo, dataSetResult3)
        )
        val resultMap = testRunFactory.getTestRuns(listOf(suiteMock))
            .first()
            .testResults
            .groupBy(
                io.github.jokoroukwu.zephyrapi.publication.TestResult::testCaseKey,
                io.github.jokoroukwu.zephyrapi.publication.TestResult::testDataResults
            )

        val expectedGroupOneSize = 2
        val expectedGroupTwoSize = 1

        with(SoftAssertions()) {
            assertThat(resultMap)
                .extracting { it!![testCaseKeyOne] }
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .singleElement()
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .hasSize(expectedGroupOneSize)

            assertThat(resultMap)
                .extracting { it!![testCaseKeyTwo] }
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .singleElement()
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .hasSize(expectedGroupTwoSize)

            assertAll()
        }
    }

    @Test
    fun `should call testDataResultFactory with expected arguments`() {
        val testDataResultMock: TimestampedTestDataResult = mockk(relaxed = true)
        val testResultMock: ITestResult = mockk(relaxed = true)
        every { suiteMock.results } returns hashMapOf(
            "1" to mockk(relaxed = true) {
                every { testContext } returns mockk(relaxed = true) {
                    every { passedTests.allResults } returns setOf(testResultMock)
                    every { failedTests.allResults } returns setOf(testResultMock)
                    every { skippedTests.allResults } returns setOf(testResultMock)
                }
            })

        every { timestampedTestDataResultFactory.getTestNgTestDataResult(any()) } returns Pair(
            "test",
            testDataResultMock
        )
        listOf(suiteMock).runCatching(testRunFactory::getTestRuns)
        verify(exactly = 3) { timestampedTestDataResultFactory.getTestNgTestDataResult(testResultMock) }
    }

    @Test
    fun `testRun should have expected planned start and end timings`() {
        val expectedStartMillis = 1L
        val controlStartMillis = 2L

        val controlEndMillis = 3L
        val expectedEndMillis = 4L

        every { suiteMock.results } returns hashMapOf(
            "1" to mockk(relaxed = true) {
                every { testContext.passedTests } returns mockk {
                    every { allResults } returns hashSetOf<ITestResult>(
                        TestResult.newEmptyTestResult().apply { status = TestResult.SUCCESS },
                        TestResult.newEmptyTestResult().apply { status = TestResult.FAILURE }
                    )
                }
            }
        )
        every { timestampedTestDataResultFactory.getTestNgTestDataResult(any()) } returnsMany listOf(
            Pair("1", TimestampedTestDataResult(controlStartMillis, expectedEndMillis, TestDataResultBase(0, true))),
            Pair("2", TimestampedTestDataResult(expectedStartMillis, controlEndMillis, TestDataResultBase(0, true)))
        )

        val testRun = testRunFactory.getTestRuns(listOf(suiteMock)).first()

        SoftAssertions().apply {
            assertThat(testRun.startTime)
                .`as`("suite suite planned start date validation")
                .isEqualTo(expectedStartMillis)

            assertThat(testRun.endTime)
                .`as`("suite planned end date validation")
                .isEqualTo(expectedEndMillis)

            assertAll()
        }
    }
}


