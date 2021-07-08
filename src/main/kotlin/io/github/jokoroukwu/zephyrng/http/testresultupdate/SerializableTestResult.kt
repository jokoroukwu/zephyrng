package io.github.jokoroukwu.zephyrng.http.testresultupdate

import kotlinx.serialization.Serializable

@Serializable
data class SerializableTestResult(
    val id: Long,
    val testResultStatusId: Long,
    val comment: String,
    val executionTime: Long
)