package io.github.jokoroukwu.zephyrng

import io.github.jokoroukwu.zephyrapi.publication.*
import io.github.jokoroukwu.zephyrng.testdataresultfactory.TimestampedTestDataResult
import io.github.jokoroukwu.zephyrng.testdataresultfactory.TimestampedTestDataResultFactory
import io.github.jokoroukwu.zephyrng.testdataresultfactory.TimestampedTestDataResultFactoryBase
import mu.KotlinLogging
import org.testng.ISuite
import org.testng.ISuiteResult
import org.testng.ITestContext
import org.testng.ITestResult
import java.util.*

private val logger = KotlinLogging.logger { }

class TestRunFactory(
    private val timestampedTestDataResultFactory: TimestampedTestDataResultFactory = TimestampedTestDataResultFactoryBase(),
) {

    fun getTestRuns(suites: List<ISuite>): List<TestRunBase> {
        return suites
            .asSequence()
            .mapNotNull(::toTestRun)
            .toCollection(ArrayList(suites.size))
            .apply(ArrayList<TestRunBase>::trimToSize)
    }

    private fun toTestRun(suite: ISuite): TestRunBase? {
        val testResultCount = suite.countTestResults()
        if (testResultCount == 0) {
            logger.debug { "'${suite.name}' suite is empty and will be ignored" }
            return null
        }
        val testCaseKeyToTestNgTestDataResult =
            HashMap<String, NavigableSet<TimestampedTestDataResult>>(testResultCount, 1F)
        return suite.results.values
            .asSequence()
            .map(ISuiteResult::getTestContext)
            .forEach { testContext -> testContext.addTestNgTestDataResults(testCaseKeyToTestNgTestDataResult) }
            .let { testCaseKeyToTestNgTestDataResult.toTestResults().toTestRun(suite) }
    }

    private fun ITestContext.addTestNgTestDataResults(testCaseKeyToTestDataResult: MutableMap<String, NavigableSet<TimestampedTestDataResult>>) {
        sequenceOf(
            failedButWithinSuccessPercentageTests.allResults,
            passedTests.allResults,
            failedTests.allResults,
            skippedTests.allResults,
        ).flatMap(Set<ITestResult>::asSequence)
            .mapNotNull(timestampedTestDataResultFactory::getTestNgTestDataResult)
            .forEach { keyToResult ->
                testCaseKeyToTestDataResult.computeIfAbsent(keyToResult.first) { TreeSet() }
                    .add(keyToResult.second)
            }
    }

    private fun Map<String, NavigableSet<TimestampedTestDataResult>>.toTestResults(): Sequence<TestResultBase> {
        return asSequence()
            .map { entry ->
                val startEndTime = entry.value.calculateTestStartEndTime()
                TestResultBase(
                    testCaseKey = entry.key,
                    startTime = startEndTime.startTime,
                    endTime = startEndTime.endTime,
                    testDataResults = Collections.unmodifiableList(entry.value.map(TimestampedTestDataResult::testDataResult))
                )
            }
    }

    private fun Sequence<TestResultBase>.toTestRun(suite: ISuite): TestRunBase? {
        return toList()
            .takeUnless(List<TestResultBase>::isEmpty)
            ?.let { testResults ->
                val startEndTime = testResults.calculateTestStartEndTime()
                TestRunBase(
                    name = suite.name,
                    testResults = Collections.unmodifiableList(testResults),
                    startTime = startEndTime.startTime,
                    endTime = startEndTime.endTime
                )
            }
    }

    private fun ISuite.countTestResults() = results.values.sumOf {
        it.testContext.passedTests.allResults.size +
                it.testContext.skippedTests.allResults.size +
                it.testContext.failedTests.allResults.size
    }

    private fun Collection<DurationRange>.calculateTestStartEndTime(): StartEndTime {
        var startTime: Long
        var endTime: Long
        val iterator = iterator()

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
        return StartEndTime(startTime, endTime)
    }

    private data class StartEndTime(val startTime: Long, val endTime: Long)
}
