package com.gmail.johnokoroukwu.zephyrng.suitetestcaseidmerger

import com.gmail.johnokoroukwu.zephyrng.http.TestNgZephyrSuite

interface TestCaseIdInjector {

    fun injectTestCaseIds(
        testSuitesWithResults: List<TestNgZephyrSuite>,
        testCaseKeyToIdMap: Map<String, Int>
    ): TestCasesIdInjectResult
}