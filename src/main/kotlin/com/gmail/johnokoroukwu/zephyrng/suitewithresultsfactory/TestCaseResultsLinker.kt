package com.gmail.johnokoroukwu.zephyrng.suitewithresultsfactory

import com.gmail.johnokoroukwu.zephyrng.TestNgDataSetResult
import org.testng.ITestResult

interface TestCaseResultsLinker {

    fun mapTestCaseKeyToDataSetResult(testResult: ITestResult): Pair<String, TestNgDataSetResult>?
}