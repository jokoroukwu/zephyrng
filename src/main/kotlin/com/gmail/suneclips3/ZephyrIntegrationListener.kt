package com.gmail.suneclips3

import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.connectionconfig.ZephyrConfigLoader
import com.gmail.suneclips3.http.ZephyrPublisher
import com.gmail.suneclips3.http.testresultstatus.TestResultToIdMapper
import com.gmail.suneclips3.suitewithresultsfactory.SuiteWithResultsFactory
import com.gmail.suneclips3.testdataprovider.TestDataInterceptor
import org.testng.*
import org.testng.xml.XmlSuite

/**
 * This class encapsulates TestNG - Zephyr integration functionality
 * in a single TestNG listener
 */
class ZephyrIntegrationListener(
    private val testDataInterceptor: TestDataInterceptor = TestDataInterceptor(),
    private val zephyrConfig: ZephyrConfig = ZephyrConfigLoader.connectionConfig(),
    private val testNgZephyrAdapter: TestNgZephyrAdapter = TestNgZephyrAdapter(
        zephyrConfig,
        ZephyrPublisher(zephyrConfig),
        ZephyrTestCaseFetcher(zephyrConfig),
        SuiteWithResultsFactory(),
        TestResultToIdMapper()
    )
) : IDataProviderInterceptor, IReporter {

    override fun intercept(
        original: MutableIterator<Array<Any>>?,
        dataProviderMethod: IDataProviderMethod?,
        method: ITestNGMethod?,
        iTestContext: ITestContext?
    ) = testDataInterceptor.intercept(original, dataProviderMethod, method, iTestContext)

    override fun generateReport(
        xmlSuites: MutableList<XmlSuite>?, suites: MutableList<ISuite>?, outputDirectory: String?
    ) = testNgZephyrAdapter.generateReport(xmlSuites, suites, outputDirectory);

}