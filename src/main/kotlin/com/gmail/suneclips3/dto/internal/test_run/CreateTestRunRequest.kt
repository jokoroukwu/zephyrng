package com.gmail.suneclips3.dto.internal.test_run

import kotlinx.serialization.Serializable

@Serializable
class CreateTestRunRequest(
    val projectId: Int,
    val name: String,
    val statusId: Int,
    val plannedStartDate: String,
    val plannedEndDate: String,
)