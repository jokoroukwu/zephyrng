package io.github.jokoroukwu.zephyrng.http.testcycleupdate

import kotlinx.serialization.Serializable

@Serializable
class TestRunItem(
    val index: Int,
    val lastTestResult: UpdateTestCycleTestResult,
    val id: Int? = null
)