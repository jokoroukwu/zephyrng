package io.github.jokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class TestRun(
    val key: String,
    val name: String
) {
}