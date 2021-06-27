package com.gmail.johnokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class TestRunDetailReport(
    val testResults: List<ZephyrTestResult>
)