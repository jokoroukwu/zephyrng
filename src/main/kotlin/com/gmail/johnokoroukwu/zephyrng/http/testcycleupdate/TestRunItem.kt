package com.gmail.johnokoroukwu.zephyrng.http.testcycleupdate

import kotlinx.serialization.Serializable

@Serializable
class TestRunItem(
    val index: Int,
    val lastTestResult: SerializableTestResult,
    val id: Int? = null
)