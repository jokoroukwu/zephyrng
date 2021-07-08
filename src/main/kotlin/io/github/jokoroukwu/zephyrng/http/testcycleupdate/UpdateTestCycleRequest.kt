package io.github.jokoroukwu.zephyrng.http.testcycleupdate

import kotlinx.serialization.Serializable

@Serializable
class UpdateTestCycleRequest(
    val testRunId: Long,
    val addedTestRunItems: List<TestRunItem>
)