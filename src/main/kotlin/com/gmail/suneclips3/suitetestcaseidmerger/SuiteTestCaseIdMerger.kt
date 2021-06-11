package com.gmail.suneclips3.suitetestcaseidmerger

import com.gmail.suneclips3.TestCaseWithTestNgResults
import com.gmail.suneclips3.http.TestSuiteWithTestCaseResults
import java.util.*

object SuiteTestCaseIdMerger : ISuiteTestCaseIdMerger {

    override fun merge(
        testSuitesWithResults: List<TestSuiteWithTestCaseResults>,
        testCaseKeyToIdMap: Map<String, Int>
    ): SuitesTestCasesMergeResult {
        val ignoredTestCaseKeys = HashSet<String>()
        val ignoredSuites = HashSet<String>()
        val filteredSuites = ArrayList<TestSuiteWithTestCaseResults>(testSuitesWithResults.size)

        testSuitesWithResults.forEach { suite ->
            with(suite.testCasesWithDataSetResults) {
                ArrayList<TestCaseWithTestNgResults>(size).also { filtered ->
                    forEach { testCase ->
                        testCaseKeyToIdMap[testCase.key]
                            ?.let { filtered.add(testCase.copy(id = it)) }
                            ?: ignoredTestCaseKeys.add(testCase.key)
                    }
                }
            }.takeUnless(List<TestCaseWithTestNgResults>::isEmpty)
                ?.also { filteredSuites.add(suite.copy(testCasesWithDataSetResults = it)) }
                ?: ignoredSuites.add(suite.name)
        }

        return SuitesTestCasesMergeResult(
            filteredSuitesWithResults = filteredSuites,
            ignoredKeys = ignoredTestCaseKeys,
            ignoredSuites = ignoredSuites
        )
    }
}