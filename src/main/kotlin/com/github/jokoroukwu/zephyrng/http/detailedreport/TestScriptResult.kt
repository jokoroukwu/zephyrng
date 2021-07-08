package com.github.jokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
data class TestScriptResult(
    val id: Long,
    val testResultStatusId: Long,
    val comment: String? = null
)