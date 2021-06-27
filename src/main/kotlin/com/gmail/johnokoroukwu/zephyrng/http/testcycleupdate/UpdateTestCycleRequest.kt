package com.gmail.johnokoroukwu.zephyrng.http.testcycleupdate

import kotlinx.serialization.Serializable

@Serializable
class UpdateTestCycleRequest(
    val testRunId: Int,
    val addedTestRunItems: List<TestRunItem>
)