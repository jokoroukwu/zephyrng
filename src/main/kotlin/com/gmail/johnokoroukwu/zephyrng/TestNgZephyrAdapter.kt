package com.gmail.johnokoroukwu.zephyrng

import com.gmail.johnokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.gmail.johnokoroukwu.zephyrng.http.AsyncZephyrPublisher
import com.gmail.johnokoroukwu.zephyrng.http.TestNgZephyrSuite
import com.gmail.johnokoroukwu.zephyrng.http.gettestcases.ZephyrTestCaseFetcher
import com.gmail.johnokoroukwu.zephyrng.http.testresultstatus.TestResultStatusToIdMapProvider
import com.gmail.johnokoroukwu.zephyrng.suitetestcaseidmerger.TestCaseIdInjector
import com.gmail.johnokoroukwu.zephyrng.suitetestcaseidmerger.TestCaseIdInjectorImpl
import com.gmail.johnokoroukwu.zephyrng.suitewithresultsfactory.TestNgZephyrSuiteFactory
import mu.KotlinLogging
import org.testng.IReporter
import org.testng.ISuite
import org.testng.xml.XmlSuite

/**
 * Collects test results' info via TestNG API and merges
 * it with fetched Zephyr data, preparing it to be published to Zephyr
 */
private val logger = KotlinLogging.logger {}

open class TestNgZephyrAdapter(
    private val projectKey: String = ZephyrNgConfigLoaderImpl.zephyrNgConfig().projectKey(),
    private val zephyrPublisher: AsyncZephyrPublisher = AsyncZephyrPublisher(),
    private val zephyrTestCaseFetcher: ZephyrTestCaseFetcher = ZephyrTestCaseFetcher(),
    private val testNgZephyrSuiteFactory: TestNgZephyrSuiteFactory = TestNgZephyrSuiteFactory(),
    private val testResultStatusToIdMapProvider: TestResultStatusToIdMapProvider = TestResultStatusToIdMapProvider(),
    private val suiteTestCaseIdInjector: TestCaseIdInjector = TestCaseIdInjectorImpl
) : IReporter {

    override fun generateReport(
        xmlSuites: MutableList<XmlSuite>?, suites: MutableList<ISuite>?, outputDirectory: String?
    ) {

        val suitesWithTestCasesResults = ArrayList<TestNgZephyrSuite>(suites!!.size)
        val testCaseKeys = HashSet<String>()
        suites.forEach { suite ->
            testNgZephyrSuiteFactory.get(suite)
                ?.also(suitesWithTestCasesResults::add)
                ?.also { it.testCasesWithDataSetResults.mapTo(testCaseKeys, TestCaseWithTestNgResults::key) }
        }

        if (testCaseKeys.isNotEmpty()) {
            zephyrTestCaseFetcher.fetchProjectWithTestCases(testCaseKeys)?.also {
                suiteTestCaseIdInjector.injectTestCaseIds(suitesWithTestCasesResults, it.testCaseKeyToIdMap)
                    .apply {
                        ignoredKeys.ifNotEmpty(::logIgnoredSuites)
                        ignoredSuites.ifNotEmpty(::logIgnoredKeys)

                    }.filteredSuitesWithResults.ifNotEmpty { suites ->
                        zephyrPublisher.submitResults(
                            projectId = it.projectId,
                            testNgZephyrSuites = suites,
                            testResultStatusToIdMap = testResultStatusToIdMapProvider.getTestResultStatusToIdMap(it.projectId)
                        )
                        return
                    }
            } ?: logger.error { "No test cases fetched from Zephyr: {project_key: $projectKey}" }
        }
        logger.info { "Nothing to publish" }
    }

    private inline fun <T> Collection<T>.ifNotEmpty(action: (Collection<T>) -> Unit): Collection<T> {
        if (isNotEmpty()) {
            action(this)
        }
        return this
    }

    private fun logIgnoredSuites(suites: Collection<String>) {
        logger.warn { "JIRA project contains no match for some test case keys: the following suites will be completely ignored: $suites}" }
    }

    private fun logIgnoredKeys(keys: Collection<String>) {
        logger.warn { "Jira project contains no match for some test case keys: test results with the following keys will be ignored $keys" }
    }
}