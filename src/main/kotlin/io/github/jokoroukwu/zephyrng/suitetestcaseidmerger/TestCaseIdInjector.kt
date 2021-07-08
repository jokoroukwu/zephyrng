package io.github.jokoroukwu.zephyrng.suitetestcaseidmerger

import io.github.jokoroukwu.zephyrng.http.TestNgZephyrSuite

interface TestCaseIdInjector {

    fun injectTestCaseIds(
        testSuitesWithResults: List<TestNgZephyrSuite>,
        testCaseKeyToIdMap: Map<String, Long>
    ): TestCasesIdInjectResult
}