package com.gmail.suneclips3.sender

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.getOrElse
import com.gmail.suneclips3.dto.ConnectionConfig
import com.gmail.suneclips3.dto.internal.test_case.GetTestCasesResponse


object GetTestCasesRequestSender {
    private const val urlTemplate = "https://%s/rest/tests/1.0/testcase/search?fields=id," +
            "key,projectId&maxResults=99999&query=testCase.key IN %s"
    private const val errorMessage = "failed to fetch test cases from Zephyr"

    fun getTestCasesRequest(
        credentials: ConnectionConfig, testCaseKeys: Collection<String>
    ): GetTestCasesResponse {
        val url = urlTemplate.format(testCaseKeys.joinToString("','", "('", "')"))

        return Fuel.get(url)
            .authentication().basic(credentials.username, credentials.password)
            .response(ZephyrResponseDeserializer)
            .third.getOrElse { cause -> throw ZephyrException(errorMessage, cause) }
            .validateStatusCode { errorMessage }
            .getJsonBody()
    }
}