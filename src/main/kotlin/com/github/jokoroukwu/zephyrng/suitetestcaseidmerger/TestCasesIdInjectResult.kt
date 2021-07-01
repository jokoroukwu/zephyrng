package com.github.jokoroukwu.zephyrng.suitetestcaseidmerger

import com.github.jokoroukwu.zephyrng.http.TestNgZephyrSuite

class TestCasesIdInjectResult(
    val filteredSuitesWithResults: List<TestNgZephyrSuite>,
    val ignoredKeys: Set<String>,
    val ignoredSuites: Set<String>
)