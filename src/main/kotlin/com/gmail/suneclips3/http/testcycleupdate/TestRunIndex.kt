package com.gmail.suneclips3.http.testcycleupdate

import kotlinx.serialization.Serializable

@Serializable
class TestRunIndex(
    val id: Int,
    val index: Int
) {
}