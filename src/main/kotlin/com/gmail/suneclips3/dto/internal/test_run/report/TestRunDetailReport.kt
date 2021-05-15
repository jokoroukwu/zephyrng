package com.gmail.suneclips3.dto.internal.test_run.report

import kotlinx.serialization.Serializable

@Serializable
class TestRunDetailReport(
    val testRun: TestRun,
    val testResults: List<TestResult>
)