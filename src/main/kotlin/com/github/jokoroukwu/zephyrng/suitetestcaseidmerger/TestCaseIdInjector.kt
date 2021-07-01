package com.github.jokoroukwu.zephyrng.suitetestcaseidmerger

import com.github.jokoroukwu.zephyrng.http.TestNgZephyrSuite

interface TestCaseIdInjector {

    fun injectTestCaseIds(
        testSuitesWithResults: List<TestNgZephyrSuite>,
        testCaseKeyToIdMap: Map<String, Int>
    ): TestCasesIdInjectResult
}