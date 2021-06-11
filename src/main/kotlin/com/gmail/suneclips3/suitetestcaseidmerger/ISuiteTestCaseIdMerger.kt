package com.gmail.suneclips3.suitetestcaseidmerger

import com.gmail.suneclips3.http.TestSuiteWithTestCaseResults

interface ISuiteTestCaseIdMerger {

    fun merge(
        testSuitesWithResults: List<TestSuiteWithTestCaseResults>,
        testCaseKeyToIdMap: Map<String, Int>
    ): SuitesTestCasesMergeResult
}