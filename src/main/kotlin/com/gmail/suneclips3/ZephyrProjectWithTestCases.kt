package com.gmail.suneclips3

data class ZephyrProjectWithTestCases(
    val projectId: Int,
    val testCaseKeyToIdMap: Map<String, Int>
)