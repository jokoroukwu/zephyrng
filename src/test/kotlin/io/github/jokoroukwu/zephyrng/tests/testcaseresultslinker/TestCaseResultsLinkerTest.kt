package io.github.jokoroukwu.zephyrng.tests.testcaseresultslinker

import io.github.jokoroukwu.zephyrng.annotations.ZephyrStep
import io.github.jokoroukwu.zephyrng.datasetindexprovider.DataSetIndexProvider
import io.github.jokoroukwu.zephyrng.failedstepprovider.FailedStepProviderImpl
import io.github.jokoroukwu.zephyrng.suitewithresultsfactory.TestCaseResultsLinker
import io.github.jokoroukwu.zephyrng.suitewithresultsfactory.TestCaseResultsLinkerImpl
import io.github.jokoroukwu.zephyrng.testcasekeyprovider.TestCaseKeyProvider
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.testng.ITestResult
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import kotlin.random.Random

class TestCaseResultsLinkerTest {

    companion object {
        @JvmStatic
        private val testResultMock: ITestResult = mockk(relaxed = true)

        @JvmStatic
        private val STEP_PROVIDER_MOCK: FailedStepProviderImpl = mockk(relaxed = true)

        @JvmStatic
        private val testCaseKeyFinderMock: TestCaseKeyProvider = mockk(relaxed = true)

        @JvmStatic
        private val dataSetIndexProviderMock: DataSetIndexProvider = mockk(relaxed = true)
    }

    private lateinit var testCaseResultsLinker: TestCaseResultsLinker

    @BeforeMethod
    fun setUp() {
        testCaseResultsLinker = TestCaseResultsLinkerImpl(
            dataSetIndexProvider = dataSetIndexProviderMock,
            failedStepProvider = STEP_PROVIDER_MOCK,
            testCaseKeyFinder = testCaseKeyFinderMock
        )
    }

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearMocks(testResultMock, STEP_PROVIDER_MOCK, testCaseKeyFinderMock, dataSetIndexProviderMock)
    }

    @Test(dataProvider = "statusProvider")
    fun `should return expected passed status`(expectedPassStatus: Boolean, dataDriven: Boolean) {
        every { testResultMock.isSuccess } returns expectedPassStatus
        every { testResultMock.method.isDataDriven } returns dataDriven

        val result = testCaseResultsLinker.mapTestCaseKeyToDataSetResult(testResultMock)
        Assertions.assertThat(result)
            .isNotNull
            .extracting { pair -> pair!!.second }
            .isNotNull

        Assertions.assertThat(result!!.second.isSuccess)
            .`as`("test result passed status validation")
            .isEqualTo(expectedPassStatus)
    }

    @DataProvider
    private fun statusProvider() = arrayOf<Array<Any>>(
        arrayOf(true, true),
        arrayOf(true, false),
        arrayOf(false, true),
        arrayOf(false, false),
    )

    @Test
    fun `should return expected start and end times`() {
        val expectedStartTime = Random.nextInt(999).toLong()
        val expectedEndTime = Random.nextInt(999).toLong()

        every { testResultMock.startMillis } returns expectedStartTime
        every { testResultMock.endMillis } returns expectedEndTime

        val resultPair = testCaseResultsLinker.mapTestCaseKeyToDataSetResult(testResultMock)
        Assertions.assertThat(resultPair).isNotNull
        Assertions.assertThat(resultPair!!.second)
            .satisfies { testCase ->
                Assertions.assertThat(testCase).`as`("test case validation").isNotNull
                with(SoftAssertions()) {
                    assertThat(testCase.startTime)
                        .`as`("start time validation")
                        .isEqualTo(expectedStartTime)

                    assertThat(testCase.endTime)
                        .`as`("end time validation")
                        .isEqualTo(expectedEndTime)

                    assertAll()
                }
            }
    }


    @Test(dataProvider = "throwableProvider")
    fun `should have expected failureMessage`(
        dataDriven: Boolean,
        throwable: Throwable?,
        expectedFailureMessage: String
    ) {
        every { testResultMock.method } returns mockk(relaxed = true) {
            every { isDataDriven } returns dataDriven
        }
        every { testResultMock.throwable } returns throwable
        val result = testCaseResultsLinker.mapTestCaseKeyToDataSetResult(testResultMock)

        Assertions.assertThat(result).`as`("result pair").isNotNull
        Assertions.assertThat(result!!.second.failureMessage)
            .`as`("failed step index validation")
            .isEqualTo(expectedFailureMessage)
    }

    @DataProvider
    private fun throwableProvider() = RuntimeException("test-failure-message").let {
        arrayOf<Array<Any?>>(
            arrayOf(true, null, ""),
            arrayOf(true, it, it.toString()),

            arrayOf(false, null, ""),
            arrayOf(false, it, it.toString())
        )
    }

    @Test(dataProvider = "keyProvider")
    fun `should return expected test case key`(expectedKey: String?) {
        every { testCaseKeyFinderMock.getTestCaseKey(any()) } returns expectedKey
        val keyToTestNgDataSet = testCaseResultsLinker.mapTestCaseKeyToDataSetResult(testResultMock)

        Assertions.assertThat(keyToTestNgDataSet?.first)
            .`as`("test case key")
            .isEqualTo(expectedKey)
    }

    @DataProvider
    private fun keyProvider() = arrayOf(
        arrayOf<Any?>("test-case-key"),
        arrayOf<Any?>(null)
    )

    @Test(dataProvider = "failedStepProvider")
    fun `should return expected failed step index`(dataDriven: Boolean, zephyrStep: ZephyrStep?) {
        every { testResultMock.method } returns mockk(relaxed = true) {
            every { isDataDriven } returns dataDriven
        }
        every { STEP_PROVIDER_MOCK.getFailedStep(any()) } returns zephyrStep
        val result = testCaseResultsLinker.mapTestCaseKeyToDataSetResult(testResultMock)

        Assertions.assertThat(result).`as`("result pair").isNotNull
        Assertions.assertThat(result!!.second.failedStepIndex)
            .`as`("failed step index validation")
            .isEqualTo(zephyrStep?.value)
    }


    @DataProvider
    private fun failedStepProvider(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf(true, mockk<ZephyrStep>(relaxed = true) {
                every { value } returns (Random.nextInt(100))
            }),
            arrayOf(true, null),
            arrayOf(false, mockk<ZephyrStep>(relaxed = true) {
                every { value } returns (Random.nextInt(100))
            }),
            arrayOf(false, null)
        )
    }

    @Test(dataProvider = "indexProvider")
    fun `should return expected data set index`(dataDriven: Boolean, expectedIndex: Int?) {
        every { testResultMock.method } returns mockk(relaxed = true) {
            every { isDataDriven } returns dataDriven
        }

        every { dataSetIndexProviderMock.pollDataSetIndex(any()) } returns expectedIndex
        val actualIndex = testCaseResultsLinker.mapTestCaseKeyToDataSetResult(testResultMock)
            ?.second?.index

        Assertions.assertThat(actualIndex)
            .`as`("data set index")
            .isEqualTo(expectedIndex)
    }


    @DataProvider
    private fun indexProvider() = arrayOf<Array<Any?>>(
        arrayOf(true, Random.nextInt(1, 100)),
        arrayOf(true, null),
        arrayOf(true, 0)
    )
}