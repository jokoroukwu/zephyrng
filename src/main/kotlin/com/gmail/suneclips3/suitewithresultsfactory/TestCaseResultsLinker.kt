package com.gmail.suneclips3.suitewithresultsfactory

import com.gmail.suneclips3.TestNgDataSet
import com.gmail.suneclips3.TestNgDataSetResult
import com.gmail.suneclips3.annotations.ZephyrStep
import com.gmail.suneclips3.stepfinder.FailedStepFinder
import com.gmail.suneclips3.stepfinder.IFailedStepFinder
import com.gmail.suneclips3.testcasekeyfinder.ITestCaseKeyFinder
import com.gmail.suneclips3.testcasekeyfinder.TestCaseKeyFinder
import com.gmail.suneclips3.testdataprovider.IDataSetIndexProvider
import com.gmail.suneclips3.testdataprovider.TestDataInterceptor
import org.testng.ITestResult

class TestCaseResultsLinker(
    private val dataSetIndexProvider: IDataSetIndexProvider = TestDataInterceptor(),
    private val failedStepFinder: IFailedStepFinder = FailedStepFinder,
    private val testCaseKeyFinder: ITestCaseKeyFinder = TestCaseKeyFinder,
) : ITestCaseResultsLinker {

    override fun mapTestCaseKeyToDataSetResult(testResult: ITestResult): Pair<String, TestNgDataSetResult>? {
        val testNgMethod = testResult.method
        val testCaseKey = testCaseKeyFinder.findTestCaseKey(testNgMethod) ?: return null
        val failedStep = failedStepFinder.findFailedStep(testResult)
        val dataSetIndex: Int?
        if (!testResult.method.isDataDriven) {
            dataSetIndex = 0
        } else {
            val dataSet = TestNgDataSet(testResult.parameters)
            dataSetIndex = dataSetIndexProvider.getDataSetIndexAndRemove(Pair(testNgMethod, dataSet))
            if (dataSetIndex == null) {
                println(
                    "WARNING: data set result will be ignored:" +
                            " {reason: no mapping for provided data set index," +
                            " test_case_key: $testCaseKey, test_method: ${testNgMethod}, data_set: $dataSet}"
                )
                return null
            }
        }

        return testCaseKey to TestNgDataSetResult(
            startTime = testResult.startMillis,
            endTime = testResult.endMillis,
            index = dataSetIndex,
            hasPassed = testResult.isSuccess,
            failedStepIndex = failedStep.logStepInfo(testCaseKey, dataSetIndex)?.value,
            failureMessage = testResult.failureMessage()
        )
    }

    private fun ITestResult.failureMessage() = throwable?.toString() ?: ""


    private fun ZephyrStep?.logStepInfo(testCaseKey: String, dataSetIndex: Int): ZephyrStep? {
        if (this != null) {
            println(
                "DEBUG found failed step: {test_case_key: $testCaseKey, description: ${description.ifEmpty { "<none>" }}," +
                        " index: $value, data_set_index: $dataSetIndex}"
            )
        }
        return this
    }
}