package com.gmail.suneclips3

import java.util.*
import kotlin.collections.HashSet

class TestCase(
    val testCaseKey: String,
    private val testResults: MutableSet<TestResult> = HashSet(10)

) {
    fun addTestCaseResult(result: TestResult) = testResults.add(result)
    fun getResults() = Collections.unmodifiableSet(testResults)
}
