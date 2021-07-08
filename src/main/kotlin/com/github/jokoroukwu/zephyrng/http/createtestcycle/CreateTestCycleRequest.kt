package com.github.jokoroukwu.zephyrng.http.createtestcycle

import kotlinx.serialization.Serializable

@Serializable
class CreateTestCycleRequest(
    val projectId: Long,
    val name: String,
    val plannedStartDate: String,
    val plannedEndDate: String
)