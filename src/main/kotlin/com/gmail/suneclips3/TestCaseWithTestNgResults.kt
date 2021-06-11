package com.gmail.suneclips3

data class TestCaseWithTestNgResults(
    val id: Int = -1,
    val key: String,
    val startTime: Long,
    val endTime: Long,
    val dataSetResults: List<TestNgDataSetResult>
)