package com.gmail.johnokoroukwu.zephyrng.http.createtestcycle

import com.gmail.johnokoroukwu.zephyrng.http.testcycleupdate.TestRunItem
import kotlinx.serialization.Serializable

@Serializable
data class CreateTestCycleResponse(
    val id: Int,
    val key: String,
    val serializableTestRunItems: List<TestRunItem> = emptyList()
)