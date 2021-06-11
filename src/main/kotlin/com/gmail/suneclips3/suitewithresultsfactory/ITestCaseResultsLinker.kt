package com.gmail.suneclips3.suitewithresultsfactory

import com.gmail.suneclips3.TestNgDataSetResult
import org.testng.ITestResult

interface ITestCaseResultsLinker {

    fun mapTestCaseKeyToDataSetResult(testResult: ITestResult): Pair<String, TestNgDataSetResult>?
}