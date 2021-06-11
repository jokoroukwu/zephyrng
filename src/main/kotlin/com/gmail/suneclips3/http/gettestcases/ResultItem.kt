package com.gmail.suneclips3.http.gettestcases

import kotlinx.serialization.Serializable

@Serializable
data class ResultItem(
    val id: Int,
    val key: String,
    val projectId: Int
)