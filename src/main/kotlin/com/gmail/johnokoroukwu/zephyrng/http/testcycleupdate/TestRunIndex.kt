package com.gmail.johnokoroukwu.zephyrng.http.testcycleupdate

import kotlinx.serialization.Serializable

@Serializable
class TestRunIndex(
    val id: Int,
    val index: Int
) {
}