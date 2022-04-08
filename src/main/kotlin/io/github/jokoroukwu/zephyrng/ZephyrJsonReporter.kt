package io.github.jokoroukwu.zephyrng

import io.github.jokoroukwu.zephyrng.datasetindexprovider.TestDataIndexProviderBase
import io.github.jokoroukwu.zephyrng.testdataresultfactory.TimestampedTestDataResultFactoryBase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.testng.*
import org.testng.xml.XmlSuite
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

const val ZEPHYR_RESULTS_DIR_PROPERTY = "zephyr.results.output.dir"

class ZephyrJsonReporter : IReporter, IDataProviderInterceptor {
    private val testRunFactory: TestRunFactory
    private val dataProviderInterceptor: IDataProviderInterceptor

    init {
        val testDataIndexProvider = TestDataIndexProviderBase()
        dataProviderInterceptor = testDataIndexProvider
        testRunFactory = TestRunFactory(
            TimestampedTestDataResultFactoryBase(testDataIndexProvider = testDataIndexProvider)
        )
    }

    override fun generateReport(xmlSuites: MutableList<XmlSuite>?, suites: MutableList<ISuite>, outputDir: String) {
        val zephyrResultsDir = System.getProperty(ZEPHYR_RESULTS_DIR_PROPERTY)
            ?: throw NoSuchElementException("'$ZEPHYR_RESULTS_DIR_PROPERTY' is not set")

        val testRuns = testRunFactory.getTestRuns(suites)
        if (testRuns.isNotEmpty()) {
            Files.createDirectories(Paths.get(zephyrResultsDir))
                .resolve("${UUID.randomUUID()}.json")
                .let {
                    Files.write(
                        it,
                        Json.encodeToString(testRuns).encodeToByteArray(),
                        StandardOpenOption.CREATE_NEW
                    )
                }
        }
    }

    override fun intercept(
        original: MutableIterator<Array<Any>>?,
        dataProviderMethod: IDataProviderMethod?,
        method: ITestNGMethod?,
        iTestContext: ITestContext?
    ): MutableIterator<Array<Any?>> =
        dataProviderInterceptor.intercept(original, dataProviderMethod, method, iTestContext)

}