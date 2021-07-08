package io.github.jokoroukwu.zephyrng.suitewithresultsfactory

import org.testng.ITestResult

interface TestCaseResultsLinker {

    fun mapTestCaseKeyToDataSetResult(testResult: ITestResult): Pair<String, io.github.jokoroukwu.zephyrng.TestNgDataSetResult>?
}