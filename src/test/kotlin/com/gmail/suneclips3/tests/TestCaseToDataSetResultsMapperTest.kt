package com.gmail.suneclips3.tests

import io.mockk.every
import io.mockk.mockk
import org.testng.IResultMap
import org.testng.ISuite
import org.testng.ISuiteResult
import org.testng.ITestContext
import java.util.*

class TestCaseToDataSetResultsMapperTest {

    fun testName() {

    }

    private fun mockResultMap(): IResultMap {
        return mockk()
    }

    private fun dataProvider(): Iterator<Any> {
        return Collections.emptyIterator()
    }

    private fun mockTestContext(
        passedTests: IResultMap = mockk(),
        skippedTests: IResultMap = mockk(),
        failedTests: IResultMap = mockk()
    ): ITestContext {

        val contextMock = mockk<ITestContext>()
        every { contextMock.passedTests } returns passedTests
        every { contextMock.skippedTests } returns skippedTests
        every { contextMock.failedTests } returns failedTests
        return contextMock
    }

    private fun mockSuiteResult(testContext: ITestContext): ISuiteResult {
        val resultMock = mockk<ISuiteResult>()
        every { resultMock.testContext } returns testContext
        return resultMock
    }

    private fun mockSuite(suiteResults: MutableCollection<ISuiteResult>): ISuite {
        val suiteMock = mockk<ISuite>()
        every { suiteMock.results.values } returns suiteResults
        return suiteMock
    }
}