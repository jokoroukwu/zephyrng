package com.github.jokoroukwu.zephyrng.http.createtestcycle

import com.github.jokoroukwu.zephyrng.http.testcycleupdate.TestRunItem
import kotlinx.serialization.Serializable

@Serializable
data class CreateTestCycleResponse(
    val id: Long,
    val key: String,
    val serializableTestRunItems: List<TestRunItem> = emptyList()
)