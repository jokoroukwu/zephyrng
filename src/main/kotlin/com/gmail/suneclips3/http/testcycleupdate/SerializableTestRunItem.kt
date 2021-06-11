package com.gmail.suneclips3.http.testcycleupdate

import kotlinx.serialization.Serializable

@Serializable
class SerializableTestRunItem(
    val index: Int,
    val lastTestResult: SerializableTestResult,
    val id: Int? = null
)