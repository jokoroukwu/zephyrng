package com.gmail.suneclips3

import java.util.*

open class TestNgTestRun(val name: String, testCaseResultsMap: MutableMap<String, TestResults>) {
    val testCaseResultMap = testCaseResultsMap
        get() = Collections.unmodifiableMap(field)

}