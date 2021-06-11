package com.gmail.suneclips3.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class TestCase(
    val id: Int,
    val key: String,
    val name: String,
)