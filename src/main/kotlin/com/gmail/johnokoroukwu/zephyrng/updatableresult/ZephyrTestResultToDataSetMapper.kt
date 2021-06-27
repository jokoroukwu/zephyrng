package com.gmail.johnokoroukwu.zephyrng.updatableresult

import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.ZephyrTestResult

typealias ZephyrDataSets = List<List<ZephyrStepResult>>

interface IZephyrTestResultToDataSetMapper {

    fun mapTestResultToZephyrDataSets(zephyrTestResult: ZephyrTestResult): ZephyrDataSets
}