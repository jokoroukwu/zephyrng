package com.gmail.suneclips3.http

import com.gmail.suneclips3.TestCaseWithTestNgResults
import java.time.Instant

data class TestSuiteWithTestCaseResults(
    val startTime: Instant,
    val endTime: Instant,
    val name: String,
    val testCasesWithDataSetResults: List<TestCaseWithTestNgResults>
)
