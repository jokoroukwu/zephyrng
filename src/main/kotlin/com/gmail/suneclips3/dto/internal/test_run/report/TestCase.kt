package com.gmail.suneclips3.dto.internal.test_run.report

import kotlinx.serialization.Serializable

@Serializable
class TestCase(
    val id: Int,
    val key: String,
    val name: String,
)