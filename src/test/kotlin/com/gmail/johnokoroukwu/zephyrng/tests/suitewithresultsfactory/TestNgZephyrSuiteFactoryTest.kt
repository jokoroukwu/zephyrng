package com.gmail.johnokoroukwu.zephyrng.tests.suitewithresultsfactory

import com.gmail.johnokoroukwu.zephyrng.TestCaseWithTestNgResults
import com.gmail.johnokoroukwu.zephyrng.TestNgDataSetResult
import com.gmail.johnokoroukwu.zephyrng.instantformatter.InstantToStringFormatterImpl
import com.gmail.johnokoroukwu.zephyrng.suitewithresultsfactory.TestCaseResultsLinker
import com.gmail.johnokoroukwu.zephyrng.suitewithresultsfactory.TestNgZephyrSuiteFactory
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

class TestNgZephyrSuiteFactoryTest {
    companion object {
        private val instantToStringFormatter = InstantToStringFormatterImpl()
        private val testCaseResultLinkerMock: TestCaseResultsLinker = mockk(relaxed = true)
        private val suiteMock: ISuite = mockk(relaxed = true)
    }

    private lateinit var testNgZephyrSuiteFactory: TestNgZephyrSuiteFactory

    @BeforeMethod(alwaysRun = true)
    fun setUp() {
        testNgZephyrSuiteFactory = TestNgZephyrSuiteFactory(testCaseResultLinkerMock)
    }

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearMocks(testCaseResultLinkerMock, suiteMock)
    }

    @AfterClass(alwaysRun = true)
    fun afterClass() {
        unmockkAll()
    }

    @Test
    fun `should ignore empty suite`() {
        every { suiteMock.results } returns emptyMap()

        val result = testNgZephyrSuiteFactory.get(suiteMock)
        Assertions.assertThat(result)
            .`as`("null suite validation")
            .isNull()
    }

    @Test
    fun `test case should have proper start and end times`() {
        val minStartTime = 1L
        val maxEndTime = 10L
        val testCaseKey = "test-case-key"
        val testResultMock = mockk<ITestResult>(relaxed = true)
        val testResultMockTwo = mockk<ITestResult>(relaxed = true)
        val dataSetResultMock = mockk<TestNgDataSetResult>(relaxed = true) {
            every { startTime } returns minStartTime
            every { endTime } returns maxEndTime - 1
        }
        val dataSetResultMockTwo = mockk<TestNgDataSetResult>(relaxed = true) {
            every { startTime } returns minStartTime + 1
            every { endTime } returns maxEndTime
        }
        every { testCaseResultLinkerMock.mapTestCaseKeyToDataSetResult(any()) } returnsMany listOf(
            Pair(testCaseKey, dataSetResultMock), Pair(testCaseKey, dataSetResultMockTwo)
        )
        every { suiteMock.results } returns hashMapOf(
            "1" to mockk(relaxed = true) {
                every { testContext } returns mockk(relaxed = true) {
                    every { passedTests.allResults } returns setOf(testResultMock, testResultMockTwo)
                }
            }
        )
        val result = testNgZephyrSuiteFactory.get(suiteMock)
        Assertions.assertThat(result)
            .`as`("test cases with results")
            .extracting { it!!.testCasesWithDataSetResults }
            .satisfies { list ->
                Assertions.assertThat(list)
                    .`as`("test case count")
                    .hasSize(1)
                Assertions.assertThat(list.first().dataSetResults)
                    .`as`("data set results count")
                    .hasSize(2)
            }

        with(SoftAssertions()) {
            assertThat(result!!.testCasesWithDataSetResults.first().startTime)
                .`as`("start time validation")
                .isEqualTo(minStartTime)

            assertThat(result.testCasesWithDataSetResults.first().endTime)
                .`as`("end time validation")
                .isEqualTo(maxEndTime)

            assertAll()
        }
    }

    @Test
    fun `test results should be grouped properly`() {
        val testResultMock = mockk<ITestResult>(relaxed = true)
        val testResultMockTwo = mockk<ITestResult>(relaxed = true)

        every { suiteMock.results } returns hashMapOf(
            "1" to mockk(relaxed = true) {
                every { testContext } returns mockk(relaxed = true) {
                    every { passedTests.allResults } returns setOf(testResultMock)
                }
            },
            "2" to mockk(relaxed = true) {
                every { testContext } returns mockk(relaxed = true) {
                    every { passedTests.allResults } returns setOf(testResultMock, testResultMockTwo)
                }
            }
        )

        val groupOne = "groupOne"
        val groupTwo = "groupTwo"

        val dataSetResultMock = mockk<TestNgDataSetResult>(relaxed = true)
        every { testCaseResultLinkerMock.mapTestCaseKeyToDataSetResult(any()) } returnsMany listOf(
            Pair(groupOne, dataSetResultMock), Pair(groupOne, dataSetResultMock), Pair(groupTwo, dataSetResultMock)
        )
        val resultMap = testNgZephyrSuiteFactory.get(suiteMock)?.testCasesWithDataSetResults
            ?.groupBy(TestCaseWithTestNgResults::key, TestCaseWithTestNgResults::dataSetResults)

        val expectedGroupOneSize = 2
        val expectedGroupTwoSize = 1

        with(SoftAssertions()) {
            assertThat(resultMap)
                .extracting { it!![groupOne] }
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .singleElement()
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .hasSize(expectedGroupOneSize)

            assertThat(resultMap)
                .extracting { it!![groupTwo] }
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .singleElement()
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .hasSize(expectedGroupTwoSize)

            assertAll()
        }
    }

    @Test
    fun `should call test result linker with proper arguments`() {
        val testResultMock = mockk<ITestResult>(relaxed = true)
        val suite = mockk<ISuite>(relaxed = true) {
            every { results } returns hashMapOf(
                "1" to mockk(relaxed = true) {
                    every { testContext } returns mockk(relaxed = true) {
                        every { passedTests.allResults } returns setOf(testResultMock)
                        every { failedTests.allResults } returns setOf(testResultMock)
                        every { skippedTests.allResults } returns setOf(testResultMock)
                    }
                })
        }
        every { testCaseResultLinkerMock.mapTestCaseKeyToDataSetResult(any()) } returns Pair("test", mockk())
        suite.runCatching(testNgZephyrSuiteFactory::get)
        verify(exactly = 3) { testCaseResultLinkerMock.mapTestCaseKeyToDataSetResult(testResultMock) }
    }

    @Test
    fun `suite should have expected planned start and end dates`() {
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
        every { testCaseResultLinkerMock.mapTestCaseKeyToDataSetResult(any()) } returnsMany listOf(
            Pair("1", TestNgDataSetResult(controlStartMillis, expectedEndMillis, 0, true)),
            Pair("2", TestNgDataSetResult(expectedStartMillis, controlEndMillis, 0, true))
        )

        val result = testNgZephyrSuiteFactory.get(suiteMock)
        Assertions.assertThat(result).`as`("test case with results").isNotNull

        SoftAssertions().apply {
            assertThat(result!!.plannedStartDate)
                .`as`("suite suite planned start date validation")
                .isEqualTo(instantToStringFormatter.formatInstant(expectedStartMillis))

            assertThat(result.plannedEndDate)
                .`as`("suite planned end date validation")
                .isEqualTo(instantToStringFormatter.formatInstant(expectedEndMillis))

            assertAll()
        }
    }
}


