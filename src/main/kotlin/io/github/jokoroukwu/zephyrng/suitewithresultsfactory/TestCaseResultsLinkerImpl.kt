package io.github.jokoroukwu.zephyrng.suitewithresultsfactory

import io.github.jokoroukwu.zephyrng.annotations.ZephyrStep
import io.github.jokoroukwu.zephyrng.datasetindexprovider.DataSetID
import io.github.jokoroukwu.zephyrng.datasetindexprovider.DataSetIndexProvider
import io.github.jokoroukwu.zephyrng.datasetindexprovider.DataSetIndexProviderImpl
import io.github.jokoroukwu.zephyrng.failedstepprovider.FailedStepProviderImpl
import io.github.jokoroukwu.zephyrng.testcasekeyprovider.TestCaseKeyProvider
import io.github.jokoroukwu.zephyrng.testcasekeyprovider.TestCaseKeyProviderImpl
import mu.KotlinLogging
import org.testng.ITestResult

private val logger = KotlinLogging.logger { }

class TestCaseResultsLinkerImpl(
    private val dataSetIndexProvider: DataSetIndexProvider = DataSetIndexProviderImpl(),
    private val failedStepProvider: FailedStepProviderImpl = FailedStepProviderImpl,
    private val testCaseKeyFinder: TestCaseKeyProvider = TestCaseKeyProviderImpl,
) : TestCaseResultsLinker {

    override fun mapTestCaseKeyToDataSetResult(testResult: ITestResult): Pair<String, io.github.jokoroukwu.zephyrng.TestNgDataSetResult>? {
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
        return testCaseKey to io.github.jokoroukwu.zephyrng.TestNgDataSetResult(
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