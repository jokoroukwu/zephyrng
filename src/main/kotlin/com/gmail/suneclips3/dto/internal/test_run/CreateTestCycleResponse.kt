package com.gmail.suneclips3.dto.internal.test_run

import com.gmail.suneclips3.dto.internal.test_run.update_test_run.SerializableTestRunItem
import kotlinx.serialization.Serializable

@Serializable
class CreateTestCycleResponse(
    val id: Int,
    val key: String,
    val serializableTestRunItems: List<SerializableTestRunItem>
) {


}