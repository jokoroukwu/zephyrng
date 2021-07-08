package com.github.jokoroukwu.zephyrng.http.gettestcases

import com.github.jokoroukwu.zephyrng.config.ZephyrConfigImpl
import com.github.jokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.github.jokoroukwu.zephyrng.http.AbstractRequestSender
import com.github.jokoroukwu.zephyrng.http.JsonMapper
import com.github.jokoroukwu.zephyrng.http.ZephyrException
import com.github.jokoroukwu.zephyrng.http.ZephyrResponseDeserializer
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import kotlinx.serialization.json.Json

const val MAX_RESULTS = 99999

class GetTestCasesRequestSender(
    config: ZephyrConfigImpl = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(config, jsonMapper, requestFactory) {
    private val urlTemplate = "$baseUrl/testcase/search?fields=id," +
            "key,projectId&maxResults=$MAX_RESULTS&query=testCase.key IN"
    private val errorMessageTemplate = "Failed to fetch test cases from Zephyr:"

    /**
     * Attempts to fetch test cases from Zephyr by provided [testCaseKeys]
     */
    fun getTestCasesRequest(testCaseKeys: Collection<String>): GetTestCasesResponse {
        val url = "$urlTemplate${testCaseKeys.joinToString("','", "('", "')")}"
        return zephyrConfig.runCatching {
            requestFactory.get(url)
                .authentication().basic(username(), password())
                .treatResponseAsValid()
                .response(ZephyrResponseDeserializer)
                .third.get()
        }.onFailure { cause -> throw ZephyrException(errorMessageTemplate, cause) }
            .mapCatching { it.getJsonBody<GetTestCasesResponse>() }
            .getOrElse { cause -> throw ZephyrException("$errorMessageTemplate: failed to deserialize body", cause) }
    }
}