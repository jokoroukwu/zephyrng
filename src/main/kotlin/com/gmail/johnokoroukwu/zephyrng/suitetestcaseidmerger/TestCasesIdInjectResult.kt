package com.gmail.johnokoroukwu.zephyrng.suitetestcaseidmerger

import com.gmail.johnokoroukwu.zephyrng.http.TestNgZephyrSuite

class TestCasesIdInjectResult(
    val filteredSuitesWithResults: List<TestNgZephyrSuite>,
    val ignoredKeys: Set<String>,
    val ignoredSuites: Set<String>
)