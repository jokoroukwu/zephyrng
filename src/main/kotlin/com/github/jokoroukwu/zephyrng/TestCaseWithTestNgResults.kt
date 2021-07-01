package com.github.jokoroukwu.zephyrng

/**
 * Contains general info of a certain test case run.
 * Non data driven test will still have single [TestNgDataSetResult] attached.
 */
data class TestCaseWithTestNgResults(
    val id: Int = -1,
    val key: String,
    val startTime: Long,
    val endTime: Long,
    val dataSetResults: List<TestNgDataSetResult>
)