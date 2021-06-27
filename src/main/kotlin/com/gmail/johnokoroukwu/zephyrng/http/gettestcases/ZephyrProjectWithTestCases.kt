package com.gmail.johnokoroukwu.zephyrng.http.gettestcases

data class ZephyrProjectWithTestCases(
    val projectId: Int,
    val testCaseKeyToIdMap: Map<String, Int>
)