package com.github.jokoroukwu.zephyrng.tests.updatableresultmapper


import com.github.jokoroukwu.zephyrng.http.TestNgZephyrSuite
import com.github.jokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import com.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrTestResult
import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.github.jokoroukwu.zephyrng.updatableresult.CommentRow
import com.github.jokoroukwu.zephyrng.updatableresult.UpdatableResultsMapper
import com.github.jokoroukwu.zephyrng.updatableresult.ZephyrTestResultToDataSetMapper
import com.github.jokoroukwu.zephyrng.updatableresult.merge.MergeResult
import com.github.jokoroukwu.zephyrng.updatableresult.merge.ResultMergeStrategy
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.Instant
import java.util.*

class UpdatableResultMapperTest {
    companion object {
        private val testResultStatusToIdMap = EnumMap<TestResultStatus, Int>(TestResultStatus::class.java).apply {
            put(TestResultStatus.BLOCKED, 1)
            put(TestResultStatus.FAIL, 2)
            put(TestResultStatus.IN_PROGRESS, 3)
            put(TestResultStatus.NOT_EXECUTED, 4)
            put(TestResultStatus.PASS, 5)
        }
        private const val testCycleKey = "test-cycle-key"
        private val testCaseWithTestNgResults = com.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults(
            id = 1,
            key = "test-case-key",
            startTime = 1L,
            endTime = 2L,
            listOf(
                com.github.jokoroukwu.zephyrng.TestNgDataSetResult(
                    index = 0,
                    startTime = 1L,
                    endTime = 2L,
                    isSuccess = true
                )
            )
        )

        private val suiteWithResults = TestNgZephyrSuite(
            Instant.now().toString(),
            Instant.now().toString(),
            "test-suite",
            listOf(testCaseWithTestNgResults)
        )

        private val mergeStrategyMock = mockk<ResultMergeStrategy>(relaxed = true)
        private val testResultToDataSetMapperMock: ZephyrTestResultToDataSetMapper = mockk()
        private val testSuiteWithResultsMock = mockk<TestNgZephyrSuite>()
        private val zephyrResultOneMock = mockk<ZephyrTestResult>()
        private val testResultTwoMock = mockk<ZephyrTestResult>()
    }

    private lateinit var updatableResultsMapper: UpdatableResultsMapper

    @BeforeMethod
    fun tearDown() {
        updatableResultsMapper = UpdatableResultsMapper(testResultToDataSetMapperMock, mergeStrategyMock)

        every { zephyrResultOneMock.id } returns 1
        every { zephyrResultOneMock.testScriptResults } returns listOf()

        every { testResultTwoMock.id } returns 2
        every { testResultTwoMock.testScriptResults } returns listOf()
    }

    @AfterMethod(alwaysRun = true)
    fun resetMocks() {
        clearMocks(
            mergeStrategyMock, testResultToDataSetMapperMock, testSuiteWithResultsMock,
            zephyrResultOneMock, testResultTwoMock
        )
    }

    @Test
    fun `should return result pair with expected values`() {
        val zephyrDataSet = listOf(ZephyrStepResult(1, 1))
        val zephyrDataSets = listOf(zephyrDataSet)

        every { zephyrResultOneMock.testCase.id } returns 1
        every {
            testResultToDataSetMapperMock.mapTestResultToZephyrDataSets(zephyrResultOneMock)
        } returns zephyrDataSets

        val expectedTestScriptResults = listOf(
            TestScriptResult(1, 2),
            TestScriptResult(2, 3)
        )
        val expectedCommentRow = CommentRow(1, TestResultStatus.FAIL)
        every { mergeStrategyMock.mergeResults(any(), any(), any()) } returns MergeResult(
            testScriptResults = expectedTestScriptResults,
            commentRow = expectedCommentRow,
            error = "test error"
        )

        val resultPair = updatableResultsMapper.mapToUpdatableResults(
            testCycleKey,
            listOf(zephyrResultOneMock),
            suiteWithResults,
            testResultStatusToIdMap
        )

        with(SoftAssertions()) {
            Assertions.assertThat(resultPair.second)
                .`as`("updatable results count validation")
                .hasSize(1)
            assertThat(resultPair.first)
                .`as`("test script results validation")
                .isEqualTo(expectedTestScriptResults)
            with(resultPair.second.first()) {
                assertThat(commentRows)
                    .`as`("comment rows validation")
                    .containsExactlyInAnyOrder(expectedCommentRow)

                assertThat(startTime)
                    .`as`("start time validation")
                    .isEqualTo(testCaseWithTestNgResults.startTime)

                assertThat(endTime)
                    .`as`("end time validation")
                    .isEqualTo(testCaseWithTestNgResults.endTime)

                assertThat(testResultId)
                    .`as`("test result id")
                    .isEqualTo(zephyrResultOneMock.id)
            }
            assertAll()
        }
    }

    @DataProvider
    private fun mergeStrategyArgProvider(): Array<Array<Any>> {
        val zephyrDataSet = listOf(ZephyrStepResult(1, 1))
        val zephyrDataSets = listOf(zephyrDataSet)
        val zephyrEmptyDataSets = listOf<List<ZephyrStepResult>>()
        return arrayOf(
            arrayOf(zephyrResultOneMock, suiteWithResults, zephyrDataSets, zephyrDataSet),
            arrayOf(zephyrResultOneMock, suiteWithResults, zephyrEmptyDataSets, emptyList<ZephyrStepResult>())
        )
    }

    @Test
    fun `should return empty result pair`() {
        every { zephyrResultOneMock.testCase.id } returns 3

        val testCaseWithTestNgResultsMockOne = mockk<com.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults> {
            every { id } returns 4
            every { key } returns "test-key-one"
        }
        val testCaseWithTestNgResultsMockTwo = mockk<com.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults> {
            every { id } returns 5
            every { key } returns "test-key-two"
        }

        every { testSuiteWithResultsMock.testCasesWithDataSetResults } returns listOf(
            testCaseWithTestNgResultsMockOne, testCaseWithTestNgResultsMockTwo
        )
        val result = updatableResultsMapper.mapToUpdatableResults(
            testCycleKey,
            listOf(zephyrResultOneMock),
            testSuiteWithResultsMock,
            testResultStatusToIdMap
        )

        with(SoftAssertions()) {
            assertThat(result.first)
                .`as`("test script results")
                .isEmpty()
            assertThat(result.second)
                .`as`("updatable test results")
                .isEmpty()

            assertAll()
        }

    }
}