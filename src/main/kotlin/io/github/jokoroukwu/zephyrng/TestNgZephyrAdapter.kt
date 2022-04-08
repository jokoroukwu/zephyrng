package io.github.jokoroukwu.zephyrng

import io.github.jokoroukwu.zephyrapi.ZephyrClient
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigLoaderImpl
import io.github.jokoroukwu.zephyrapi.publication.TestRun
import io.github.jokoroukwu.zephyrng.datasetindexprovider.TestDataIndexProviderBase
import org.testng.IDataProviderInterceptor
import org.testng.IReporter
import org.testng.ISuite
import org.testng.xml.XmlSuite

/**
 * A bridge between [ZephyrClient] and [IReporter].
 *
 * This TestNG listener can be used for 'out-of-the-box' functionality.
 */
open class TestNgZephyrAdapter(
    private val dataProviderInterceptor: IDataProviderInterceptor = TestDataIndexProviderBase(),
    private val testRunFactory: TestRunFactory = TestRunFactory(),
    private val zephyrClient: ZephyrClient = ZephyrClient
) : IReporter, IDataProviderInterceptor by dataProviderInterceptor {

    override fun generateReport(xmlSuites: MutableList<XmlSuite>?, suites: MutableList<ISuite>, outputDir: String?) {
        val testRuns: List<TestRun> = testRunFactory.getTestRuns(suites)
        zephyrClient.publishTestResults(testRuns, ZephyrConfigLoaderImpl.getZephyrConfig())
    }

}