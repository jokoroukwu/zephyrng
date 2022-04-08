package io.github.jokoroukwu.zephyrng.testdataresultfactory

import io.github.jokoroukwu.zephyrapi.annotations.Step
import io.github.jokoroukwu.zephyrapi.publication.TestDataResultBase
import io.github.jokoroukwu.zephyrng.datasetindexprovider.TestDataID
import io.github.jokoroukwu.zephyrng.datasetindexprovider.TestDataIndexProvider
import io.github.jokoroukwu.zephyrng.datasetindexprovider.TestDataIndexProviderBase
import io.github.jokoroukwu.zephyrng.failedstepprovider.FailedStepProviderImpl
import io.github.jokoroukwu.zephyrng.testcasekeyprovider.TestCaseKeyProvider
import io.github.jokoroukwu.zephyrng.testcasekeyprovider.TestCaseKeyProviderImpl
import mu.KotlinLogging
import org.testng.ITestResult

private val logger = KotlinLogging.logger { }

class TimestampedTestDataResultFactoryBase(
    private val testDataIndexProvider: TestDataIndexProvider = TestDataIndexProviderBase(),
    private val failedStepProvider: FailedStepProviderImpl = FailedStepProviderImpl,
    private val testCaseKeyFinder: TestCaseKeyProvider = TestCaseKeyProviderImpl,
) : TimestampedTestDataResultFactory {

    override fun getTestNgTestDataResult(testResult: ITestResult): Pair<String, TimestampedTestDataResult>? {
        val testNgMethod = testResult.method
        val testCaseKey = testCaseKeyFinder.getTestCaseKey(testNgMethod) ?: return null
        val testDataIndex: Int? = testResult.testDataIndex()
        if (testDataIndex == null) {
            logger.warn {
                "Test data result will be ignored: no mapping for test data: " +
                        "{test_case_key: '$testCaseKey', test_method: '${testResult.method}', parameters: '${testResult.parameters}'}"
            }
            return null
        }
        return testCaseKey to getTestDataResult(testResult, testDataIndex, testCaseKey)
    }

    private fun ITestResult.testDataIndex(): Int? {
        val testNgMethod = method
        if (method.isDataDriven) {
            val testDataId = TestDataID(testNgMethod, parameters)
            return testDataIndexProvider.pollDataSetIndex(testDataId)
        }
        return 0
    }

    private fun getTestDataResult(
        testResult: ITestResult,
        testDataIndex: Int,
        testCaseKey: String
    ): TimestampedTestDataResult {
        val failedStep = if (testResult.isSuccess) null else failedStepProvider.getFailedStep(testResult)
        return TimestampedTestDataResult(
            startTime = testResult.startMillis,
            endTime = testResult.endMillis,
            testDataResult = TestDataResultBase(
                index = testDataIndex,
                isSuccess = testResult.isSuccess,
                failedStepIndex = failedStep.logIfNotNull(testCaseKey, testDataIndex)?.value,
                failureMessage = testResult.failureMessage()
            )
        )
    }

    private fun ITestResult.failureMessage() = throwable?.toString() ?: ""

    private fun Step?.logIfNotNull(testCaseKey: String, dataSetIndex: Int) =
        this?.apply {
            logger.debug {
                "found failed step: {step: {index: $value, description: $description}, test_case_key: $testCaseKey, " +
                        "data_set_index: $dataSetIndex}"
            }
        }

}