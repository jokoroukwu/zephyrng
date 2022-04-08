package io.github.jokoroukwu.zephyrng.testdataresultfactory

import org.testng.ITestResult

interface TimestampedTestDataResultFactory {

    fun getTestNgTestDataResult(testResult: ITestResult): Pair<String, TimestampedTestDataResult>?
}