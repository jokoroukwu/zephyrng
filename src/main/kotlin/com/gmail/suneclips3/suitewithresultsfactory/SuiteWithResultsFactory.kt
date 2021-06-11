package com.gmail.suneclips3.suitewithresultsfactory

import com.gmail.suneclips3.TestCaseWithTestNgResults
import com.gmail.suneclips3.TestNgDataSetResult
import com.gmail.suneclips3.http.TestSuiteWithTestCaseResults
import com.gmail.suneclips3.suiteexecutionstate.SuiteAttribute
import org.testng.ISuite
import org.testng.ITestResult
import java.time.Instant
import java.util.*
import java.util.stream.Stream

class SuiteWithResultsFactory(
    private val testCaseResultsLinker: ITestCaseResultsLinker = TestCaseResultsLinker()
) {

    fun get(suite: ISuite): TestSuiteWithTestCaseResults {
        val capacity = suite.results.values.sumOf {
            it.testContext.passedTests.allResults.size +
                    it.testContext.skippedTests.allResults.size +
                    it.testContext.failedTests.allResults.size
        }
        val testRunResultMap = HashMap<String, MutableList<TestNgDataSetResult>>(capacity * 3, 1F)
        for (suiteResult in suite.results.values) {
            val testContext = suiteResult.testContext
            Stream.of(
                testContext.passedTests.allResults,
                testContext.failedTests.allResults,
                testContext.skippedTests.allResults
            )
                .flatMap(Set<ITestResult>::stream)
                .map(testCaseResultsLinker::mapTestCaseKeyToDataSetResult)
                .filter(Objects::nonNull)
                .forEach { keyToResult ->
                    testRunResultMap.computeIfAbsent(keyToResult!!.first) { ArrayList(5) }.add(keyToResult.second)
                }
        }

        return TestSuiteWithTestCaseResults(
            startTime = suite.getStartTime(),
            endTime = suite.getEndTime(),
            name = suite.resolveSuiteName(),
            testCasesWithDataSetResults = toTestCaseWithResult(testRunResultMap)
        )
    }


    private fun toTestCaseWithResult(map: Map<String, MutableList<TestNgDataSetResult>>) =
        map.entries.mapTo(ArrayList(map.size)) { entry ->
            with(getStartEndTime(entry.value)) {
                TestCaseWithTestNgResults(
                    key = entry.key,
                    startTime = first,
                    endTime = second,
                    dataSetResults = entry.value
                )
            }
        }

    private fun ISuite.resolveSuiteName() = if (name.isNullOrEmpty()) "Anonymous-suite" else name

    private fun ISuite.getStartTime() = getAttribute(SuiteAttribute.START_TIME) as Instant

    private fun ISuite.getEndTime() = getAttribute(SuiteAttribute.END_TIME) as Instant

    private fun getStartEndTime(testNgDataSetResults: MutableList<TestNgDataSetResult>): Pair<Long, Long> {
        if (testNgDataSetResults.isEmpty()) {
            throw NoSuchElementException("empty TestNGResult list")
        }
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
