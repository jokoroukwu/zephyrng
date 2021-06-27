package com.gmail.johnokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class ZephyrTestResult(
    val id: Int,
    val testCase: TestCase,
    val testScriptResults: List<ZephyrStepResult>
)
