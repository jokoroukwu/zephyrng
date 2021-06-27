package com.gmail.johnokoroukwu.zephyrng.suitewithresultsfactory

import com.gmail.johnokoroukwu.zephyrng.TestNgDataSetResult
import com.gmail.johnokoroukwu.zephyrng.annotations.ZephyrStep
import com.gmail.johnokoroukwu.zephyrng.datasetindexprovider.DataSetID
import com.gmail.johnokoroukwu.zephyrng.datasetindexprovider.DataSetIndexProvider
import com.gmail.johnokoroukwu.zephyrng.datasetindexprovider.DataSetIndexProviderImpl
import com.gmail.johnokoroukwu.zephyrng.failedstepprovider.FailedStepProviderImpl
import com.gmail.johnokoroukwu.zephyrng.testcasekeyprovider.TestCaseKeyProvider
import com.gmail.johnokoroukwu.zephyrng.testcasekeyprovider.TestCaseKeyProviderImpl
import mu.KotlinLogging
import org.testng.ITestResult

private val logger = KotlinLogging.logger { }

class TestCaseResultsLinkerImpl(
    private val dataSetIndexProvider: DataSetIndexProvider = DataSetIndexProviderImpl(),
    private val failedStepProvider: FailedStepProviderImpl = FailedStepProviderImpl,
    private val testCaseKeyFinder: TestCaseKeyProvider = TestCaseKeyProviderImpl,
) : TestCaseResultsLinker {

    override fun mapTestCaseKeyToDataSetResult(testResult: ITestResult): Pair<String, TestNgDataSetResult>? {
        val testNgMethod = testResult.method
        val testCaseKey = testCaseKeyFinder.getTestCaseKey(testNgMethod) ?: return null
        val dataSetIndex: Int?
        if (!testResult.method.isDataDriven) {
            dataSetIndex = 0
        } else {
            val dataSetID = DataSetID(testNgMethod, testResult.parameters)
            dataSetIndex = dataSetIndexProvider.pollDataSetIndex(dataSetID)
            if (dataSetIndex == null) {
                logger.warn {
                    "Ignoring data set result: no mapping for provided dataset index: {" +
                            "test_case_key: '$testCaseKey', dataset_id: $dataSetID}"
                }
                return null
            }
        }
        val failedStep = if (testResult.isSuccess) null else failedStepProvider.getFailedStep(testResult)
        return testCaseKey to TestNgDataSetResult(
            startTime = testResult.startMillis,
            endTime = testResult.endMillis,
            index = dataSetIndex,
            isSuccess = testResult.isSuccess,
            failedStepIndex = failedStep.logIfNotNull(testCaseKey, dataSetIndex)?.value,
            failureMessage = testResult.failureMessage()
        )
    }

    private fun ITestResult.failureMessage() = throwable?.toString() ?: ""

    private fun ZephyrStep?.logIfNotNull(testCaseKey: String, dataSetIndex: Int) =
        this?.apply {
            logger.debug {
                "failed step: {step: {index: $value, description: $description}, test_case_key: $testCaseKey, " +
                        "data_set_index: $dataSetIndex}"
            }
        }

}