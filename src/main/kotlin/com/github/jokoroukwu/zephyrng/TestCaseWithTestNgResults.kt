package com.github.jokoroukwu.zephyrng

/**
 * Encapsulates data of a certain test case run.
 * Non data driven test will still have a single [TestNgDataSetResult] attached.
 *
 * @param id test case id from JIRA server
 * @param key test case key
 * @param startTime test start time
 * @param endTime test end time
 * @param dataSetResults test results corresponding to that test case
 */
data class TestCaseWithTestNgResults(
    val id: Long = -1,
    val key: String,
    val startTime: Long,
    val endTime: Long,
    val dataSetResults: List<TestNgDataSetResult>
)