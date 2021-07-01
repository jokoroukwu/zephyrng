package com.github.jokoroukwu.zephyrng

import com.github.jokoroukwu.zephyrng.datasetindexprovider.DataSetIndexProviderImpl
import com.github.jokoroukwu.zephyrng.suitewithresultsfactory.TestCaseResultsLinkerImpl
import com.github.jokoroukwu.zephyrng.suitewithresultsfactory.TestNgZephyrSuiteFactory
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
        xmlSuites: MutableList<XmlSuite>?, suites: MutableList<ISuite>, outputDirectory: String?
    ) = testNgZephyrAdapter.generateReport(xmlSuites, suites, outputDirectory)
}