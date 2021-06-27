package com.gmail.johnokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
data class TestScriptResult(
    val id: Int,
    val testResultStatusId: Int,
    val comment: String? = null
)