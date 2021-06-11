package com.gmail.suneclips3.suitetestcaseidmerger

import com.gmail.suneclips3.http.TestSuiteWithTestCaseResults

class SuitesTestCasesMergeResult(
    val filteredSuitesWithResults: List<TestSuiteWithTestCaseResults>,
    val ignoredKeys: Set<String>,
    val ignoredSuites: Set<String>
)