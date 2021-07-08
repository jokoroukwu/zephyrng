package com.github.jokoroukwu.zephyrng.http.gettestcases

import kotlinx.serialization.Serializable

@Serializable
data class ResultItem(
    val id: Long,
    val key: String,
    val projectId: Long
)