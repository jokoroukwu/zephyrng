package com.gmail.johnokoroukwu.zephyrng.updatableresult

import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.ZephyrTestResult


object ZephyrResultToDataSetMapperImpl : IZephyrTestResultToDataSetMapper {


    override fun mapTestResultToZephyrDataSets(zephyrTestResult: ZephyrTestResult): ZephyrDataSets {
        val datasets = ArrayList<List<ZephyrStepResult>>(5)
        var nextDataSet = ArrayList<ZephyrStepResult>()

        var currentIndex = -1
        for (zephyrStep in zephyrTestResult.testScriptResults) {
            if (!nextDataSet.addIfNextIndexFollows(zephyrStep, currentIndex++)) {
                datasets.add(nextDataSet)

                nextDataSet = ArrayList()
                nextDataSet.add(zephyrStep)
                currentIndex = -1
            }
        }
        if (nextDataSet.isNotEmpty()) {
            datasets.add(nextDataSet)
        }
        return datasets
    }

    private fun MutableList<ZephyrStepResult>.addIfNextIndexFollows(
        nextStep: ZephyrStepResult,
        currentIndex: Int
    ): Boolean {
        val follows = nextStep.index > currentIndex
        if (follows) {
            add(nextStep)
        }
        return follows
    }
}