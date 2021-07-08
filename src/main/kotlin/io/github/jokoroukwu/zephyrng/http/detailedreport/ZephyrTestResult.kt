package io.github.jokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class ZephyrTestResult(
    val id: Long,
    val testCase: TestCase,
    val testScriptResults: List<ZephyrStepResult>
)
