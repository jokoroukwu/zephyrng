package com.gmail.suneclips3

import java.util.*
import kotlin.collections.ArrayList

class TestResults {
    private val results: MutableList<TestResult> = ArrayList(5)

    fun getAllResults() = Collections.unmodifiableList(results)
    fun addResult(testResult: TestResult) = results.add(testResult)
}