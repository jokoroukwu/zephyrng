package io.github.jokoroukwu.zephyrng.suitetestcaseidmerger

import io.github.jokoroukwu.zephyrng.http.TestNgZephyrSuite

class TestCasesIdInjectResult(
    val filteredSuitesWithResults: List<TestNgZephyrSuite>,
    val ignoredKeys: Set<String>,
    val ignoredSuites: Set<String>
)