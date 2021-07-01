package com.github.jokoroukwu.zephyrng.http.testcycleupdate

import kotlinx.serialization.Serializable

@Serializable
class TestRunIndex(
    val id: Int,
    val index: Int
) {
}