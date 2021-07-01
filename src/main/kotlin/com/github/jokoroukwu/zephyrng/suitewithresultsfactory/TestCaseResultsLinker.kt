package com.github.jokoroukwu.zephyrng.suitewithresultsfactory

import org.testng.ITestResult

interface TestCaseResultsLinker {

    fun mapTestCaseKeyToDataSetResult(testResult: ITestResult): Pair<String, com.github.jokoroukwu.zephyrng.TestNgDataSetResult>?
}