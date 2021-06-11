package com.gmail.suneclips3.tests.suitewithresultsfactory

import com.gmail.suneclips3.TestCaseWithTestNgResults
import com.gmail.suneclips3.TestNgDataSetResult
import com.gmail.suneclips3.suiteexecutionstate.SuiteAttribute
import com.gmail.suneclips3.suitewithresultsfactory.ITestCaseResultsLinker
import com.gmail.suneclips3.suitewithresultsfactory.SuiteWithResultsFactory
import io.mockk.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.InstanceOfAssertFactories
import org.assertj.core.api.SoftAssertions
import org.testng.ISuite
import org.testng.ITestResult
import org.testng.annotations.*
import java.time.Instant

class SuiteWithResultsFactoryTest {
    companion object {
        private val testCaseResultLinkerMock: ITestCaseResultsLinker = mockk(relaxed = true)
        private val suiteMock: ISuite = mockk(relaxed = true)
    }

    private lateinit var suiteWithResultsFactory: SuiteWithResultsFactory
    private lateinit var startTime: Instant
    private lateinit var endTime: Instant

    @BeforeMethod(alwaysRun = true)
    fun setUp() {
        startTime = Instant.now()
        endTime = startTime.plusSeconds(5)

        every { suiteMock.getAttribute(SuiteAttribute.START_TIME) } returns startTime
        every { suiteMock.getAttribute(SuiteAttribute.END_TIME) } returns endTime

        suiteWithResultsFactory = SuiteWithResultsFactory(testCaseResultLinkerMock)
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
        val result = suiteWithResultsFactory.get(suiteMock)
        Assertions.assertThat(result.testCasesWithDataSetResults)
            .`as`("test cases with results")
            .satisfies { list ->
                Assertions.assertThat(list)
                    .`as`("test case count")
                    .hasSize(1)
                Assertions.assertThat(list.first().dataSetResults)
                    .`as`("data set results count")
                    .hasSize(2)
            }

        with(SoftAssertions()) {
            assertThat(result.testCasesWithDataSetResults.first().startTime)
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

        every { suiteMock.getAttribute(any()) } returnsMany listOf(startTime, endTime)
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
        val resultMap = suiteWithResultsFactory.get(suiteMock).testCasesWithDataSetResults
            .groupBy(TestCaseWithTestNgResults::key, TestCaseWithTestNgResults::dataSetResults)

        val expectedGroupOneSize = 2
        val expectedGroupTwoSize = 1

        with(SoftAssertions()) {
            assertThat(resultMap[groupOne])
                .singleElement()
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .hasSize(expectedGroupOneSize)

            assertThat(resultMap[groupTwo])
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
            every { getAttribute(any()) } returnsMany listOf(startTime, endTime)
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
        suite.runCatching(suiteWithResultsFactory::get)
        verify(exactly = 3) { testCaseResultLinkerMock.mapTestCaseKeyToDataSetResult(testResultMock) }
    }


    @Test(dataProvider = "suiteAndNameProvider")
    fun `suite name should be resolved properly`(suite: ISuite, expectedName: String) {
        val result = suiteWithResultsFactory.get(suite)
        Assertions.assertThat(result.name)
            .`as`("suite name validation")
            .isEqualTo(expectedName)
    }

    @Test
    fun `suite should have expected start and end times`() {
        val suite = mockk<ISuite>(relaxed = true) {
            every { getAttribute(any()) } returnsMany listOf(startTime, endTime)
        }
        val result = suiteWithResultsFactory.get(suite)
        Assertions.assertThat(result).`as`("test case with results").isNotNull

        SoftAssertions().apply {
            assertThat(result.startTime)
                .`as`("suite start time validation")
                .isEqualTo(startTime)

            assertThat(result.endTime)
                .`as`("suite end time validation")
                .isEqualTo(endTime)

            assertAll()
        }
    }


    @DataProvider
    private fun suiteAndNameProvider(): Array<Array<Any>> {
        val suiteName = "test-suite"
        val suite = mockk<ISuite>(relaxed = true) {
            every { getAttribute(SuiteAttribute.START_TIME) } returns startTime
            every { getAttribute(SuiteAttribute.END_TIME) } returns endTime
            every { name } returnsMany listOf("", null, suiteName)
        }
        return arrayOf(
            arrayOf(suite, "Anonymous-suite"),
            arrayOf(suite, "Anonymous-suite"),
            arrayOf(suite, suiteName)
        )
    }
}


