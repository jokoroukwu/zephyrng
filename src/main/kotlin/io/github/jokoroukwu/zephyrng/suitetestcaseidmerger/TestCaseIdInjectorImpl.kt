package io.github.jokoroukwu.zephyrng.suitetestcaseidmerger

import io.github.jokoroukwu.zephyrng.http.TestNgZephyrSuite
import java.util.*

object TestCaseIdInjectorImpl : TestCaseIdInjector {

    override fun injectTestCaseIds(
        testSuitesWithResults: List<TestNgZephyrSuite>,
        testCaseKeyToIdMap: Map<String, Long>
    ): TestCasesIdInjectResult {
        val ignoredTestCaseKeys = HashSet<String>()
        val ignoredSuites = HashSet<String>()
        val filteredSuites = ArrayList<TestNgZephyrSuite>(testSuitesWithResults.size)

        testSuitesWithResults.forEach { suite ->
            with(suite.testCasesWithDataSetResults) {
                ArrayList<io.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults>(size).also { filtered ->
                    forEach { testCase ->
                        testCaseKeyToIdMap[testCase.key]
                            ?.let { filtered.add(testCase.copy(id = it)) }
                            ?: ignoredTestCaseKeys.add(testCase.key)
                    }
                }
            }.takeUnless(List<io.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults>::isEmpty)
                ?.also { filteredSuites.add(suite.copy(testCasesWithDataSetResults = it)) }
                ?: ignoredSuites.add(suite.name)
        }

        return TestCasesIdInjectResult(
            filteredSuitesWithResults = filteredSuites,
            ignoredKeys = ignoredTestCaseKeys,
            ignoredSuites = ignoredSuites
        )
    }
}