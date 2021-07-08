package io.github.jokoroukwu.zephyrng.suitewithresultsfactory

import io.github.jokoroukwu.zephyrng.http.TestNgZephyrSuite
import io.github.jokoroukwu.zephyrng.instantformatter.InstantToStringFormatterImpl
import mu.KotlinLogging
import org.testng.ISuite
import org.testng.ITestResult
import java.time.Instant
import java.util.*
import java.util.stream.Stream

typealias TestCaseToResults = HashMap<String, MutableList<io.github.jokoroukwu.zephyrng.TestNgDataSetResult>>

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
            .takeUnless(List<io.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults>::isEmpty)
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


    private fun toTestCasesWithResults(map: Map<String, MutableList<io.github.jokoroukwu.zephyrng.TestNgDataSetResult>>): List<io.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults> {
        if (map.isEmpty()) {
            return emptyList()
        }
        return map.entries.mapTo(ArrayList(map.size)) { entry ->
            with(calculateTestStartEndTime(entry.value)) {
                io.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults(
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

    private fun List<io.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults>.maxEndTime(): Instant {
        return Instant.ofEpochMilli(maxOf(io.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults::endTime))
    }


    private fun List<io.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults>.minStartTime(): Instant {
        return Instant.ofEpochMilli(minOf(io.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults::startTime))
    }

    private fun calculateTestStartEndTime(testNgDataSetResults: MutableList<io.github.jokoroukwu.zephyrng.TestNgDataSetResult>): Pair<Long, Long> {
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
