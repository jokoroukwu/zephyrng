package com.gmail.suneclips3.dto.internal.test_run.update_test_run

import kotlinx.serialization.Serializable

@Serializable
class SerializableTestRunItem(
    val index: Int,
    val lastTestResult: SerializableTestResult,
    val id: Int? = null
)