package com.gmail.suneclips3

import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.http.ZephyrException
import com.gmail.suneclips3.http.gettestcases.GetTestCasesRequestSender
import com.gmail.suneclips3.http.gettestcases.ResultItem
import java.util.*

class ZephyrTestCaseFetcher(
    private val zephyrConfig: ZephyrConfig,
    private val getTestCasesRequestSender: GetTestCasesRequestSender = GetTestCasesRequestSender(zephyrConfig),
) {

    /**
     * Attempts to fetch all test cases from Zephyr by their keys
     */
    fun fetchProjectWithTestCases(testCaseKeys: Collection<String>): ZephyrProjectWithTestCases {
        val projectId: Int
        val testCaseToIdMap = getTestCasesRequestSender.getTestCasesRequest(testCaseKeys)
            .results
            .ifEmpty { throw ZephyrException("No test cases fetched from Zephyr: {project: ${zephyrConfig.projectKey()}}") }
            .apply { projectId = first().projectId }
            .run(::mapTestCasesToId)

        return ZephyrProjectWithTestCases(projectId, testCaseToIdMap)
    }

    private fun mapTestCasesToId(resultItems: Collection<ResultItem>): Map<String, Int> {
        return Collections.unmodifiableMap(resultItems.associateTo(HashMap(resultItems.size, 1F))
        { it.key to it.id })
    }
}