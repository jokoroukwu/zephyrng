package com.gmail.suneclips3.updatableresultsmapper

import com.gmail.suneclips3.http.detailedreport.DetailedReportTestScriptResult
import com.gmail.suneclips3.http.detailedreport.TestResult


class ZephyrResultToDataSetMapper : IZephyrTestResultToDataSetMapper {

    override fun mapTestResultToZephyrDataSets(testResult: TestResult): ZephyrDataSet {
        val datasets = ArrayList<List<DetailedReportTestScriptResult>>(5)
        var nextDataSet = ArrayList<DetailedReportTestScriptResult>(10)

        var currentIndex = -1
        for (testScriptResult in testResult.testScriptResults) {
            if (!nextDataSet.addIf(testScriptResult) { index > currentIndex }) {
                datasets.add(nextDataSet)

                nextDataSet = ArrayList(10)
                nextDataSet.add(testScriptResult)
                currentIndex = 0
            }
        }
        return datasets
    }

    private inline fun MutableList<DetailedReportTestScriptResult>.addIf(
        result: DetailedReportTestScriptResult,
        resultPredicate: DetailedReportTestScriptResult.() -> Boolean
    ): Boolean {

        val satisfied = resultPredicate.invoke(result)
        return if (satisfied) {
            add(result)
            satisfied
        } else {
            satisfied
        }
    }
}