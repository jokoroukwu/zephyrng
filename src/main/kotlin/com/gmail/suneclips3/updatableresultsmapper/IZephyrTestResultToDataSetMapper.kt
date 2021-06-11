package com.gmail.suneclips3.updatableresultsmapper

import com.gmail.suneclips3.http.detailedreport.DetailedReportTestScriptResult
import com.gmail.suneclips3.http.detailedreport.TestResult

typealias ZephyrDataSet = List<List<DetailedReportTestScriptResult>>

interface IZephyrTestResultToDataSetMapper {

    fun mapTestResultToZephyrDataSets(testResult: TestResult): ZephyrDataSet
}