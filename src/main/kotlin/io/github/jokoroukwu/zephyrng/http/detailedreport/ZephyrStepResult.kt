package io.github.jokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
data class ZephyrStepResult(val id: Long, val index: Int)