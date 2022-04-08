package io.github.jokoroukwu.zephyrng.testngtestdataresultfactory

import io.github.jokoroukwu.zephyrapi.annotations.Step
import io.github.jokoroukwu.zephyrng.datasetindexprovider.TestDataIndexProvider
import io.github.jokoroukwu.zephyrng.failedstepprovider.FailedStepProviderImpl
import io.github.jokoroukwu.zephyrng.testcasekeyprovider.TestCaseKeyProvider
import io.github.jokoroukwu.zephyrng.testdataresultfactory.TimestampedTestDataResultFactory
import io.github.jokoroukwu.zephyrng.testdataresultfactory.TimestampedTestDataResultFactoryBase
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

class TestNgTestDataResultFactoryTest {

    companion object {
        @JvmStatic
        private val testResultMock: ITestResult = mockk(relaxed = true)

        @JvmStatic
        private val failedStepProviderMock: FailedStepProviderImpl = mockk(relaxed = true)

        @JvmStatic
        private val testCaseKeyFinderMock: TestCaseKeyProvider = mockk(relaxed = true)

        @JvmStatic
        private val testDataIndexProviderMock: TestDataIndexProvider = mockk(relaxed = true)
    }

    private lateinit var timestampedTestDataResultFactory: TimestampedTestDataResultFactory

    @BeforeMethod
    fun setUp() {
        timestampedTestDataResultFactory = TimestampedTestDataResultFactoryBase(
            testDataIndexProvider = testDataIndexProviderMock,
            failedStepProvider = failedStepProviderMock,
            testCaseKeyFinder = testCaseKeyFinderMock
        )
    }

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearMocks(testResultMock, failedStepProviderMock, testCaseKeyFinderMock, testDataIndexProviderMock)
    }

    @Test(dataProvider = "statusProvider")
    fun `should return expected passed status`(expectedPassStatus: Boolean, dataDriven: Boolean) {
        every { testResultMock.isSuccess } returns expectedPassStatus
        every { testResultMock.method.isDataDriven } returns dataDriven

        val testCaseKeyToTestDataResult = timestampedTestDataResultFactory.getTestNgTestDataResult(testResultMock)!!
        Assertions.assertThat(testCaseKeyToTestDataResult.second)
            .isNotNull
        Assertions.assertThat(testCaseKeyToTestDataResult.second.testDataResult.isSuccess)
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

        val testCaseKeyToTestDataResult = timestampedTestDataResultFactory.getTestNgTestDataResult(testResultMock)
        Assertions.assertThat(testCaseKeyToTestDataResult)
            .isNotNull
        Assertions.assertThat(testCaseKeyToTestDataResult!!.second)
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
        val testCaseKeyToTestDataResult = timestampedTestDataResultFactory.getTestNgTestDataResult(testResultMock)!!

        Assertions.assertThat(testCaseKeyToTestDataResult.second.testDataResult.failureMessage)
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

    @DataProvider
    private fun keyProvider() = arrayOf(
        arrayOf<Any?>("test-case-key"),
        arrayOf<Any?>(null)
    )

    @Test(dataProvider = "keyProvider")
    fun `should return expected test case key`(expectedKey: String?) {
        every { testCaseKeyFinderMock.getTestCaseKey(any()) } returns expectedKey
        val keyToTestNgDataSet = timestampedTestDataResultFactory.getTestNgTestDataResult(testResultMock)

        Assertions.assertThat(keyToTestNgDataSet?.first)
            .`as`("test case key")
            .isEqualTo(expectedKey)
    }

    @DataProvider
    private fun failedStepProvider(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf(true, mockk<Step>(relaxed = true) {
                every { value } returns (Random.nextInt(100))
            }),
            arrayOf(true, null),
            arrayOf(false, mockk<Step>(relaxed = true) {
                every { value } returns (Random.nextInt(100))
            }),
            arrayOf(false, null)
        )
    }


    @Test(dataProvider = "failedStepProvider")
    fun `should return expected failed step index`(dataDriven: Boolean, zephyrStep: Step?) {
        every { testResultMock.method } returns mockk(relaxed = true) {
            every { isDataDriven } returns dataDriven
        }
        every { failedStepProviderMock.getFailedStep(any()) } returns zephyrStep
        val testCaseKeyToTestDataResult = timestampedTestDataResultFactory.getTestNgTestDataResult(testResultMock)!!

        Assertions.assertThat(testCaseKeyToTestDataResult).`as`("result pair").isNotNull
        Assertions.assertThat(testCaseKeyToTestDataResult.second.testDataResult.failedStepIndex)
            .`as`("failed step index validation")
            .isEqualTo(zephyrStep?.value)
    }

    @DataProvider
    private fun indexProvider() = arrayOf<Array<Any?>>(
        arrayOf(true, Random.nextInt(1, 100)),
        arrayOf(true, null),
        arrayOf(true, 0),
        arrayOf(false, 0)
    )


    @Test(dataProvider = "indexProvider")
    fun `should return expected test data index`(dataDriven: Boolean, expectedIndex: Int?) {
        every { testResultMock.method } returns mockk(relaxed = true) {
            every { isDataDriven } returns dataDriven
        }

        every { testDataIndexProviderMock.pollDataSetIndex(any()) } returns expectedIndex
        val actualIndex = timestampedTestDataResultFactory.getTestNgTestDataResult(testResultMock)
            ?.second
            ?.testDataResult
            ?.index

        Assertions.assertThat(actualIndex)
            .`as`("test data index")
            .isEqualTo(expectedIndex)
    }
}