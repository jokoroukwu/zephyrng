package io.github.jokoroukwu.zephyrng.updatableresult

import io.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import io.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrTestResult

typealias ZephyrDataSets = List<List<ZephyrStepResult>>

interface ZephyrTestResultToDataSetMapper {

    fun mapTestResultToZephyrDataSets(zephyrTestResult: ZephyrTestResult): ZephyrDataSets
}