package com.gmail.suneclips3.testing

import com.gmail.suneclips3.TestNgDataSetResult
import com.gmail.suneclips3.suitewithresultsfactory.SuiteWithResultsFactory
import com.gmail.suneclips3.testdataprovider.TestDataInterceptor
import org.testng.*
import org.testng.xml.XmlSuite
import java.time.Instant

class DummyListener : IDataProviderInterceptor, IReporter {
    private val dataProviderInterceptor = TestDataInterceptor()
    private val testCaseToDataSetResultsMapper = SuiteWithResultsFactory()

    override fun intercept(
        original: MutableIterator<Array<Any>>?,
        dataProviderMethod: IDataProviderMethod?,
        method: ITestNGMethod?,
        iTestContext: ITestContext?
    ): MutableIterator<Array<Any>>? {
        return dataProviderInterceptor.intercept(original, dataProviderMethod, method, iTestContext)
    }

    override fun generateReport(
        xmlSuites: MutableList<XmlSuite>?,
        suites: MutableList<ISuite>?,
        outputDirectory: String?
    ) {
        val testSuitesWithDataSetResults = ArrayList<Pair<String, Map<String, List<TestNgDataSetResult>>>>()


        for (suite in suites!!) {
            val testSuiteName = "${resolveSuiteName(suite)}-${Instant.now()}"
            val testCaseKeyToDataSetResultMap = testCaseToDataSetResultsMapper.get(suite);
        }
    }

    private fun resolveSuiteName(suite: ISuite) =
        if (suite.name.isNullOrEmpty()) "Anonymous-suite" else suite.name

}