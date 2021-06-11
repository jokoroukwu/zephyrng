package com.gmail.suneclips3.http.gettestcases

import kotlinx.serialization.Serializable

@Serializable
data class GetTestCasesResponse(
    val total: Int,
    val startAt: Int,
    val maxResults: Int,
    val results: List<ResultItem> = emptyList()
)