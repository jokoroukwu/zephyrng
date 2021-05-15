package com.gmail.suneclips3.testng

import com.gmail.suneclips3.TestNgTestRun
import com.gmail.suneclips3.ZephyrTestCycle
import com.gmail.suneclips3.ZephyrTestResult
import com.gmail.suneclips3.sender.ZephyrProjectTestCases
import com.gmail.suneclips3.sender.ZephyrPublisher
import com.gmail.suneclips3.sender.ZephyrTestCaseFetcher
import org.testng.IReporter
import org.testng.ISuite
import org.testng.xml.XmlSuite
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * Aggregates all necessary test info provided by TestNG API
 * preparing it to be published to Zephyr
 */
open class TestNgZephyrAdapter(
    private val zephyrPublisher: ZephyrPublisher = ZephyrPublisher(ConnectionConfigLoader.loadCredentials()),
    private val zephyrTestCaseFetcher: ZephyrTestCaseFetcher = ZephyrTestCaseFetcher(),
    private val suiteToTestNgRunMapper: SuiteToTestNgRunMapper = SuiteToTestNgRunMapper(),
) : IReporter {

    override fun generateReport(
        xmlSuites: MutableList<XmlSuite>?, suites: MutableList<ISuite>?, outputDirectory: String?
    ) {
        val testNgTestRuns = ArrayList<TestNgTestRun>()
        for (suite in suites!!) {
            testNgTestRuns.add(suiteToTestNgRunMapper.mapSuiteToTestNgTestRun(suite))
        }
        val testCaseKeys = testNgTestRuns.flatMapTo(HashSet()) { run -> run.testCaseResultMap.keys }

        if (testCaseKeys.isNotEmpty()) {
            val zephyrProjectTestCases = zephyrTestCaseFetcher.fetchProjectTestCases(testCaseKeys)
            val filteredTestNgTestRuns = filterTestNgTestRunsAndLogResults(testNgTestRuns, zephyrProjectTestCases)

            if (filteredTestNgTestRuns.isNotEmpty()) {
                val zephyrTestCycles = mapToZephyrTestCycles(filteredTestNgTestRuns, zephyrProjectTestCases)
                zephyrPublisher.submitTestResults(zephyrTestCycles)
                return
            }
        }
        println("nothing to publish")
    }

    private fun mapToZephyrTestCycles(
        testNgTestRuns: List<TestNgTestRun>,
        zephyrProjectTestCases: ZephyrProjectTestCases
    ): List<ZephyrTestCycle> {

        val zephyrTestCycles = ArrayList<ZephyrTestCycle>(testNgTestRuns.size)
        val testCaseKeyToIdMap = zephyrProjectTestCases.testCaseKeyToIdMap;

        for (testNgTestRun in testNgTestRuns) {
            val zephyrTestResults = ArrayList<ZephyrTestResult>(testNgTestRun.testCaseResultMap.size)
            for (entry in testNgTestRun.testCaseResultMap) {
                val zephyrTestResult = ZephyrTestResult(
                    requireNotNull(testCaseKeyToIdMap[entry.key]) { "test case id" },
                    entry.key,
                    entry.value
                )
                zephyrTestResults.add(zephyrTestResult)
            }
            zephyrTestCycles.add(
                ZephyrTestCycle(
                    testNgTestRun.name,
                    zephyrProjectTestCases.projectId,
                    zephyrTestResults
                )
            )

        }
        return Collections.unmodifiableList(zephyrTestCycles)
    }

    private fun filterTestNgTestRunsAndLogResults(
        testNgTestRuns: List<TestNgTestRun>,
        zephyrProjectTestCases: ZephyrProjectTestCases
    ): List<TestNgTestRun> {
        val initiallyEmptyTestRuns = ArrayList<String>()
        val testRunsWithPartlyFetchedTestCases = HashMap<String, Set<String>>()
        val testRunsWithNoFetchedTestCases = ArrayList<String>()

        val filteredTestNgRuns = filterTestNgTestRuns(
            testNgTestRuns,
            zephyrProjectTestCases,
            initiallyEmptyTestRuns,
            testRunsWithPartlyFetchedTestCases,
            testRunsWithNoFetchedTestCases
        )
        logFilterResults(initiallyEmptyTestRuns, testRunsWithNoFetchedTestCases, testRunsWithPartlyFetchedTestCases)
        return filteredTestNgRuns
    }

    private fun filterTestNgTestRuns(
        testNgTestRuns: List<TestNgTestRun>,
        zephyrProjectTestCases: ZephyrProjectTestCases,
        initiallyEmptyTestRuns: MutableList<String>,
        testRunsWithPartlyFetchedTestCases: MutableMap<String, Set<String>>,
        testRunsWithNoFetchedTestCases: MutableList<String>

    ): List<TestNgTestRun> {
        val actualTestNgTestRuns = ArrayList<TestNgTestRun>(testNgTestRuns.size)
        val testCaseKeyToIdMap = zephyrProjectTestCases.testCaseKeyToIdMap

        for (testRun in testNgTestRuns) {
            val testCaseResultMap = testRun.testCaseResultMap
            if (testCaseResultMap.isNotEmpty()) {
                val unobtainedKeys = HashSet<String>()
                val filteredTestCaseResultMap = testCaseResultMap.filterTo(HashMap(testCaseResultMap.size)) { entry ->
                    if (testCaseKeyToIdMap.containsKey(entry.key)) {
                        true
                    } else {
                        unobtainedKeys.add(entry.key)
                        false
                    }
                }

                when {
                    unobtainedKeys.isEmpty() -> actualTestNgTestRuns.add(testRun)
                    filteredTestCaseResultMap.isEmpty() -> testRunsWithNoFetchedTestCases.add(testRun.name)
                    else -> {
                        testRunsWithPartlyFetchedTestCases[testRun.name] = unobtainedKeys
                        actualTestNgTestRuns.add(TestNgTestRun(testRun.name, filteredTestCaseResultMap))
                    }
                }
            } else {
                initiallyEmptyTestRuns.add(testRun.name)
            }
        }
        return actualTestNgTestRuns
    }

    private fun logFilterResults(
        initiallyEmptyTestRuns: List<String>,
        testRunsWithNoFetchedTestCases: List<String>,
        testRunsWithPartlyFetchedTestCases: Map<String, Set<String>>
    ) {
        if (initiallyEmptyTestRuns.isNotEmpty()) {
            println(
                "WARN some test runs will be completely ignored: {\n\treason: no testNG results,\n\t" +
                        "test_runs_names: $initiallyEmptyTestRuns\n}"
            )
        }

        if (testRunsWithNoFetchedTestCases.isNotEmpty()) {
            println(
                "WARN some test runs will be completely ignored: {\n\treason: no test cases fetched from Zephyr,\n\t" +
                        "test_runs_names: $testRunsWithNoFetchedTestCases\n}"
            )
        }

        if (testRunsWithPartlyFetchedTestCases.isNotEmpty()) {
            val stringRepresentation = testRunsWithPartlyFetchedTestCases.entries.joinToString(
                prefix = "[\n\t\t",
                postfix = "\n\t\t]",
                separator = ",\n\t\t"
            ) { entry -> "{test_run_name: $entry.key, test_cases_keys: ${entry.value}}" }
            println(
                "WARN some test results will not be published {\n\treason: no test cases fetched from Zephyr,\n\t" +
                        "test_runs: $stringRepresentation"
            )
        }
    }

/*
    private fun findFailedStep2(testResult: ITestResult): Int? {
        val testClass = testResult.testClass.realClass
        val stackTraceElements = testResult.throwable.stackTrace
        var stepIndex: Int? = null
        var i = 0
        var currentElement: StackTraceElement
        var currentClass: Class<*>
        while (!stackTraceElements.let {
                currentElement = it[i++]
                currentClass = Class.forName(currentElement.className)
                isWithinTestClassScope(testClass, currentClass)
            }) {
            val step = walkClassMethods(currentClass, currentElement.methodName)
            if (step != null) {
                stepIndex = step.value
            }
        }
        do {
            val step = walkClassMethods(currentClass, currentElement.methodName)
            if (step != null) {
                stepIndex = step.value
            }

        } while (stackTraceElements.let {
                currentElement = it[i++]
                currentClass = Class.forName(currentElement.className)
                isWithinTestClassScope(testClass, currentClass)
            })
        return stepIndex
    }*/


}