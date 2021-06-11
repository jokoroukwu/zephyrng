package com.gmail.suneclips3.http.testcycleupdate

import kotlinx.serialization.Serializable

@Serializable
class UpdateTestCycleRequest(
    val testRunId: Int,
    val addedSerializableTestRunItems: List<SerializableTestRunItem>,
    val updatedSerializableTestRunItems: List<SerializableTestRunItem> = emptyList(),
    val deletedSerializableTestRunItems: List<SerializableTestRunItem> = emptyList(),
    val updatedTestRunIndexes: List<TestRunIndex> = emptyList()
) {
}