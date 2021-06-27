package com.gmail.johnokoroukwu.zephyrng.http.gettestcases

import java.util.*

class ZephyrTestCaseFetcher(
    private val getTestCasesRequestSender: GetTestCasesRequestSender = GetTestCasesRequestSender(),
) {

    /**
     * Returns a container with Zephyr project id,
     * where provided test case keys are mapped to their Zephyr ids or null if there are no matches
     *
     * @param testCaseKeys test case keys to be mapped with corresponding ids
     */
    fun fetchProjectWithTestCases(testCaseKeys: Collection<String>): ZephyrProjectWithTestCases? {
        return getTestCasesRequestSender.getTestCasesRequest(testCaseKeys)
            .results
            .takeUnless(List<ResultItem>::isEmpty)
            ?.let { ZephyrProjectWithTestCases(it.first().projectId, mapTestCasesToId(it)) }
    }

    private fun mapTestCasesToId(resultItems: Collection<ResultItem>): Map<String, Int> {
        return Collections.unmodifiableMap(resultItems.associateTo(HashMap(resultItems.size, 1F))
        { it.key to it.id })
    }
}