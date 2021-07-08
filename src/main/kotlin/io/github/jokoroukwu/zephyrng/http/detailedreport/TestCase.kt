package io.github.jokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class TestCase(
    val id: Long,
    val key: String,
    val name: String,
)