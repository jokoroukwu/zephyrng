package com.gmail.suneclips3.testng

import com.charleskorn.kaml.Yaml
import com.gmail.suneclips3.dto.ConnectionConfig
import com.gmail.suneclips3.sender.ZephyrPublisher
import com.gmail.suneclips3.sender.ZephyrTestCaseFetcher
import org.testng.*
import org.testng.xml.XmlSuite
import java.io.FileNotFoundException
import kotlin.time.ExperimentalTime

/**
 * The coordinator between other
 * listeners responsible for Zephyr-TestNG integration
 */
class ZephyrIntegrationListener(testDataInterceptor: TestDataInterceptor = TestDataInterceptor()) :
    IDataProviderInterceptor, IReporter {
    companion object {
        private val testDataInterceptor = TestDataInterceptor()
        private val zephyrReporter = TestNgZephyrAdapter(
            ZephyrPublisher(ConnectionConfigLoader.loadCredentials()),
            ZephyrTestCaseFetcher(),
            SuiteToTestNgRunMapper(testDataInterceptor)
        )
    }

    override fun intercept(
        original: MutableIterator<Array<Any>>?,
        dataProviderMethod: IDataProviderMethod?,
        method: ITestNGMethod?,
        iTestContext: ITestContext?
    ) = testDataInterceptor.intercept(original, dataProviderMethod, method, iTestContext)

    @ExperimentalTime
    override fun generateReport(
        xmlSuites: MutableList<XmlSuite>?, suites: MutableList<ISuite>?, outputDirectory: String?
    ) = zephyrReporter.generateReport(xmlSuites, suites, outputDirectory);

}