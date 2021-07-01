package com.github.jokoroukwu.zephyrng.suitewithresultsfactory

import com.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults
import com.github.jokoroukwu.zephyrng.http.TestNgZephyrSuite
import com.github.jokoroukwu.zephyrng.instantformatter.InstantToStringFormatterImpl
import mu.KotlinLogging
import org.testng.ISuite
import org.testng.ITestResult
import java.time.Instant
import java.util.*
import java.util.stream.Stream

typealias TestCaseToResults = HashMap<String, MutableList<com.github.jokoroukwu.zephyrng.TestNgDataSetResult>>

private val logger = KotlinLogging.logger { }

class TestNgZephyrSuiteFactory(
    private val testCaseResultsLinker: TestCaseResultsLinker = TestCaseResultsLinkerImpl(),
    private val instantToStringFormatter: InstantToStringFormatterImpl = InstantToStringFormatterImpl()
) {

    fun get(suite: ISuite): TestNgZephyrSuite? {
        val testResultCount = suite.countTestResults()
        if (testResultCount == 0) {
            logger.debug { "Ignoring empty suite: ${suite.name}" }
            return null
        }
        val testCaseWithResults = TestCaseToResults(testResultCount, 1F)
        for (suiteResult in suite.results.values) {
            with(suiteResult.testContext) {
                Stream.of(
                    passedTests.allResults,
                    failedTests.allResults,
                    skippedTests.allResults
                )
            }
                .flatMap(Set<ITestResult>::stream)
                .map(testCaseResultsLinker::mapTestCaseKeyToDataSetResult)
                .filter(Objects::nonNull)
                .forEach { keyToResult ->
                    testCaseWithResults.computeIfAbsent(keyToResult!!.first) { ArrayList(5) }.add(keyToResult.second)
                }
        }
        return toTestCasesWithResults(testCaseWithResults)
            .takeUnless(List<TestCaseWithTestNgResults>::isEmpty)
            ?.let {
                val suiteStartTime = it.minStartTime()
                val suiteEndTime = it.maxEndTime()

                TestNgZephyrSuite(
                    plannedStartDate = instantToStringFormatter.formatInstant(suiteStartTime),
                    plannedEndDate = instantToStringFormatter.formatInstant(suiteEndTime),
                    name = "${suite.name} ${suiteNamePostfix(suiteStartTime, suiteEndTime)}",
                    testCasesWithDataSetResults = it
                )
            }
    }


    private fun toTestCasesWithResults(map: Map<String, MutableList<com.github.jokoroukwu.zephyrng.TestNgDataSetResult>>): List<TestCaseWithTestNgResults> {
        if (map.isEmpty()) {
            return emptyList()
        }
        return map.entries.mapTo(ArrayList(map.size)) { entry ->
            with(calculateTestStartEndTime(entry.value)) {
                TestCaseWithTestNgResults(
                    key = entry.key,
                    startTime = first,
                    endTime = second,
                    dataSetResults = entry.value
                )
            }
        }
    }

    private fun suiteNamePostfix(startTime: Instant, endTime: Instant): String {
        return "{time: ${instantToStringFormatter.formatInstant(startTime)} - ${
            instantToStringFormatter.formatInstant(endTime)
        }}"
    }

    private fun ISuite.countTestResults() = results.values.sumOf {
        it.testContext.passedTests.allResults.size +
                it.testContext.skippedTests.allResults.size +
                it.testContext.failedTests.allResults.size
    }

    private fun List<TestCaseWithTestNgResults>.maxEndTime(): Instant {
        return Instant.ofEpochMilli(maxOf(TestCaseWithTestNgResults::endTime))
    }


    private fun List<TestCaseWithTestNgResults>.minStartTime(): Instant {
        return Instant.ofEpochMilli(minOf(TestCaseWithTestNgResults::startTime))
    }

    private fun calculateTestStartEndTime(testNgDataSetResults: MutableList<com.github.jokoroukwu.zephyrng.TestNgDataSetResult>): Pair<Long, Long> {
        var startTime: Long
        var endTime: Long
        val iterator = testNgDataSetResults.iterator()

        var next = iterator.next()
        startTime = next.startTime
        endTime = next.endTime

        while (iterator.hasNext()) {
            next = iterator.next()
            if (next.startTime < startTime) {
                startTime = next.startTime
            }
            if (next.endTime > endTime) {
                endTime = next.endTime
            }
        }
        return startTime to endTime
    }
}
