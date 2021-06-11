package com.gmail.suneclips3.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class TestResult(
    val id: Int,
    val testCase: TestCase,
    val testScriptResults: List<DetailedReportTestScriptResult>
)
