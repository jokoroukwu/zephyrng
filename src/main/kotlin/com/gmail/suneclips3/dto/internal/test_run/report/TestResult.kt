package com.gmail.suneclips3.dto.internal.test_run.report

import kotlinx.serialization.Serializable

@Serializable
class TestResult(
    val id: Int,
    val lastResult: Boolean,
    val testCase: TestCase,
    val testScriptResults: List<TestScriptResult>
)
