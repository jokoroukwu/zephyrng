package com.gmail.johnokoroukwu.zephyrng.http

import com.gmail.johnokoroukwu.zephyrng.TestCaseWithTestNgResults

data class TestNgZephyrSuite(
    val plannedStartDate: String,
    val plannedEndDate: String,
    val name: String,
    val testCasesWithDataSetResults: List<TestCaseWithTestNgResults>
)
