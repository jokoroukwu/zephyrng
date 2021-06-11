package com.gmail.suneclips3.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class TestRunDetailReport(
    val testRun: TestRun,
    val testResults: List<TestResult>
)