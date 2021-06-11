package com.gmail.suneclips3.http.gettestcases

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.http.AbstractRequestSender
import com.gmail.suneclips3.http.JsonMapper
import com.gmail.suneclips3.http.ZephyrException
import com.gmail.suneclips3.http.ZephyrResponseDeserializer
import kotlinx.serialization.json.Json


class GetTestCasesRequestSender(
    config: ZephyrConfig,
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = Fuel
) : AbstractRequestSender(config, jsonMapper, requestFactory) {
    private val urlTemplate = "$baseUrl/testcase/search?fields=id," +
            "key,projectId&maxResults=99999&query=testCase.key IN"
    private val errorMessageTemplate = "failed to fetch test cases from Zephyr"

    /**
     * Attempts to fetch all test cases from Zephyr by their keys
     */
    fun getTestCasesRequest(testCaseKeys: Collection<String>): GetTestCasesResponse {
        val url = "$urlTemplate ${testCaseKeys.joinToString("','", "('", "')")}"
        return kotlin.runCatching {
            requestFactory.get(url)
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .response(ZephyrResponseDeserializer)
        }.getOrElse { cause -> throw ZephyrException(errorMessageTemplate, cause) }
            .third.get()
            .validateStatusCode { "$errorMessageTemplate: unsuccessful status code" }
            .runCatching { getJsonBody<GetTestCasesResponse>() }
            .getOrElse { cause -> throw ZephyrException("$errorMessageTemplate: deserialization error", cause) }

    }
}