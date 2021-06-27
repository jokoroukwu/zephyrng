package com.gmail.johnokoroukwu.zephyrng

import com.gmail.johnokoroukwu.zephyrng.datasetindexprovider.DataSetIndexProviderImpl
import com.gmail.johnokoroukwu.zephyrng.suitewithresultsfactory.TestCaseResultsLinkerImpl
import com.gmail.johnokoroukwu.zephyrng.suitewithresultsfactory.TestNgZephyrSuiteFactory
import org.testng.*
import org.testng.xml.XmlSuite

/**
 * Combines [DataSetIndexProviderImpl] and [TestNgZephyrAdapter] in a single TestNG listener.
 * Adding this listener to TestNG may be just enough for 'out-of-the-box' functionality.
 */
class ZephyrNgListener(
    private val dataSetIndexProvider: DataSetIndexProviderImpl = DataSetIndexProviderImpl(),
    private val testNgZephyrAdapter: TestNgZephyrAdapter = TestNgZephyrAdapter(
        testNgZephyrSuiteFactory = TestNgZephyrSuiteFactory(
            testCaseResultsLinker = TestCaseResultsLinkerImpl(dataSetIndexProvider)
        )
    )
) : IDataProviderInterceptor, IReporter {

    override fun intercept(
        original: MutableIterator<Array<Any?>>?,
        dataProviderMethod: IDataProviderMethod?,
        method: ITestNGMethod?,
        iTestContext: ITestContext?
    ) = dataSetIndexProvider.intercept(original, dataProviderMethod, method, iTestContext)

    override fun generateReport(
        xmlSuites: MutableList<XmlSuite>?, suites: MutableList<ISuite>?, outputDirectory: String?
    ) = testNgZephyrAdapter.generateReport(xmlSuites, suites, outputDirectory)
}