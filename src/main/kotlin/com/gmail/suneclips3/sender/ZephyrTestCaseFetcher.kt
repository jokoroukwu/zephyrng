package com.gmail.suneclips3.sender

import com.gmail.suneclips3.dto.ConnectionConfig
import com.gmail.suneclips3.dto.internal.test_case.ResultItem
import com.gmail.suneclips3.testng.ConnectionConfigLoader

class ZephyrTestCaseFetcher(
    private val connectionConfig: ConnectionConfig = ConnectionConfigLoader.loadCredentials(),
    private val getTestCasesRequestSender: GetTestCasesRequestSender = GetTestCasesRequestSender,
) {


    fun fetchProjectTestCases(testCaseKeys: Collection<String>): ZephyrProjectTestCases {
        val projectId: Int
        val testCaseToIdMap = getTestCasesRequestSender.getTestCasesRequest(connectionConfig, testCaseKeys)
            .results
            .ifEmpty { throw ZephyrException("No test cases fetched from Zephyr: {project: ${connectionConfig.projectKey}}") }
            .apply { projectId = first().projectId }
            .run(::mapTestCasesToId)

        return ZephyrProjectTestCases(projectId, testCaseToIdMap)
    }

    private fun mapTestCasesToId(resultItems: Collection<ResultItem>): Map<String, Int> {
        return resultItems.associateTo(HashMap()) { result -> result.key to result.id }
    }
}