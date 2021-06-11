package com.gmail.suneclips3

import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.connectionconfig.ZephyrConfigLoader
import com.gmail.suneclips3.http.TestSuiteWithTestCaseResults
import com.gmail.suneclips3.http.ZephyrPublisher
import com.gmail.suneclips3.http.testresultstatus.TestResultToIdMapper
import com.gmail.suneclips3.suitetestcaseidmerger.ISuiteTestCaseIdMerger
import com.gmail.suneclips3.suitetestcaseidmerger.SuiteTestCaseIdMerger
import com.gmail.suneclips3.suitewithresultsfactory.SuiteWithResultsFactory
import org.testng.IReporter
import org.testng.ISuite
import org.testng.xml.XmlSuite

/**
 * Aggregates all necessary test info provided by TestNG API
 * preparing it to be published to Zephyr
 */
open class TestNgZephyrAdapter(
    private val zephyrConfig: ZephyrConfig = ZephyrConfigLoader.connectionConfig(),
    private val zephyrPublisher: ZephyrPublisher = ZephyrPublisher(zephyrConfig),
    private val zephyrTestCaseFetcher: ZephyrTestCaseFetcher = ZephyrTestCaseFetcher(zephyrConfig),
    private val suiteWithResultsFactory: SuiteWithResultsFactory = SuiteWithResultsFactory(),
    private val testResultStatusToIdMapper: TestResultToIdMapper = TestResultToIdMapper(),
    private val suiteTestCaseIdMerger: ISuiteTestCaseIdMerger = SuiteTestCaseIdMerger
) : IReporter {

    override fun generateReport(
        xmlSuites: MutableList<XmlSuite>?, suites: MutableList<ISuite>?, outputDirectory: String?
    ) {
        val suitesWithTestCases = ArrayList<TestSuiteWithTestCaseResults>(suites!!.size)
        val testCaseKeys = HashSet<String>()
        suites.forEach { suite ->
            suiteWithResultsFactory.get(suite)
                .also(suitesWithTestCases::add)
                .apply { testCasesWithDataSetResults.mapTo(testCaseKeys, TestCaseWithTestNgResults::key) }
        }

        if (testCaseKeys.isNotEmpty()) {
            with(zephyrTestCaseFetcher.fetchProjectWithTestCases(testCaseKeys)) {
                suiteTestCaseIdMerger.merge(suitesWithTestCases, testCaseKeyToIdMap)
                    .apply {
                        ignoredKeys.applyIfNotEmpty(::logIgnoredSuites)
                        ignoredSuites.applyIfNotEmpty(::logIgnoredKeys)

                    }.filteredSuitesWithResults.applyIfNotEmpty {
                        zephyrPublisher.submitResults(
                            projectId = projectId,
                            suitesWithTestCaseResults = it,
                            testResultStatusToIdMap = testResultStatusToIdMapper.getTestResultStatusToIdMap(projectId)
                        )
                        return
                    }
            }
        }
        println("nothing to publish")
    }

    private inline fun <T> Collection<T>.applyIfNotEmpty(action: (Collection<T>) -> Unit): Collection<T> {
        if (isEmpty()) {
            action.invoke(this)
        }
        return this
    }

    private fun logIgnoredSuites(suites: Collection<String>) {
        println("WARN completely ignoring suites: no test case keys match Zephyr test case keys: $suites}")
    }

    private fun logIgnoredKeys(keys: Collection<String>) {
        println("WARN ignoring tests: test case keys do not match Zephyr test case keys: $keys")
    }
}