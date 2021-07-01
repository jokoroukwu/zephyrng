package com.github.jokoroukwu.zephyrng.http

data class TestNgZephyrSuite(
    val plannedStartDate: String,
    val plannedEndDate: String,
    val name: String,
    val testCasesWithDataSetResults: List<com.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults>
)
