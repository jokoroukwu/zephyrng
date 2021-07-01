package com.github.jokoroukwu.zephyrng.http.gettestcases

import kotlinx.serialization.Serializable

@Serializable
data class ResultItem(
    val id: Int,
    val key: String,
    val projectId: Int
)