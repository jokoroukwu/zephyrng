package com.github.jokoroukwu.zephyrng.http.gettestcases

data class ZephyrProjectWithTestCases(
    val projectId: Long,
    val testCaseKeyToIdMap: Map<String, Long>
)