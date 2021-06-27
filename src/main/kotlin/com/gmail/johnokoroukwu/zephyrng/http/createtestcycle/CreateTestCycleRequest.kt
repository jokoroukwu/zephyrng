package com.gmail.johnokoroukwu.zephyrng.http.createtestcycle

import kotlinx.serialization.Serializable

@Serializable
class CreateTestCycleRequest(
    val projectId: Int,
    val name: String,
    val plannedStartDate: String,
    val plannedEndDate: String,
)