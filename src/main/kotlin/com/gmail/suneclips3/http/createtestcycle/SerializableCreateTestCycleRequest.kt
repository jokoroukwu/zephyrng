package com.gmail.suneclips3.http.createtestcycle

import kotlinx.serialization.Serializable

@Serializable
class SerializableCreateTestCycleRequest(
    val projectId: Int,
    val name: String,
    val plannedStartDate: String,
    val plannedEndDate: String,
)