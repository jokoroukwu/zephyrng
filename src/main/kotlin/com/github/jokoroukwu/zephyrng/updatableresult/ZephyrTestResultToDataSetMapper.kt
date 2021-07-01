package com.github.jokoroukwu.zephyrng.updatableresult

import com.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrTestResult

typealias ZephyrDataSets = List<List<ZephyrStepResult>>

interface ZephyrTestResultToDataSetMapper {

    fun mapTestResultToZephyrDataSets(zephyrTestResult: ZephyrTestResult): ZephyrDataSets
}