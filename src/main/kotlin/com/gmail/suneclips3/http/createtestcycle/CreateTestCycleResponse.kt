package com.gmail.suneclips3.http.createtestcycle

import com.gmail.suneclips3.http.testcycleupdate.SerializableTestRunItem
import kotlinx.serialization.Serializable

@Serializable
class CreateTestCycleResponse(
    val id: Int,
    val key: String,
    val serializableTestRunItems: List<SerializableTestRunItem>
) {


}