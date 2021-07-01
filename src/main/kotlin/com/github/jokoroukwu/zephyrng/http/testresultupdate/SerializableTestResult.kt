package com.github.jokoroukwu.zephyrng.http.testresultupdate

import kotlinx.serialization.Serializable

@Serializable
data class SerializableTestResult(
    val id: Int,
    val testResultStatusId: Int,
    val comment: String,
    val executionTime: Long
)