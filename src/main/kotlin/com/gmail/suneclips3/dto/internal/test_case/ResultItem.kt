package com.gmail.suneclips3.dto.internal.test_case

import kotlinx.serialization.Serializable

@Serializable
data class ResultItem(
    val id: Int,
    val key: String,
    val projectId: Int
)