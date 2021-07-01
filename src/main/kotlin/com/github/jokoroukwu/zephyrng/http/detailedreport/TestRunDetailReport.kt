package com.github.jokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class TestRunDetailReport(
    val testResults: List<ZephyrTestResult>
)