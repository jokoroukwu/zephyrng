package com.gmail.johnokoroukwu.zephyrng.http.testscriptresult

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.gmail.johnokoroukwu.zephyrng.config.ZephyrConfigImpl
import com.gmail.johnokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.gmail.johnokoroukwu.zephyrng.http.AbstractRequestSender
import com.gmail.johnokoroukwu.zephyrng.http.JsonMapper
import com.gmail.johnokoroukwu.zephyrng.http.ZephyrException
import com.gmail.johnokoroukwu.zephyrng.http.ZephyrResponseDeserializer
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.TestScriptResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UpdateTestScriptResultsRequestSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "$baseUrl/testscriptresult"
    private val errorMessageTemplate = "Failed to update test script results: {test_cycle_key: %s}"

    suspend fun updateTestScriptResults(testCycleKey: String, testScriptResults: List<TestScriptResult>) {
        requestFactory.runCatching {
            put(url).authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .jsonBody(jsonMapper.encodeToString(testScriptResults))
                .treatResponseAsValid()
                .await(ZephyrResponseDeserializer)
        }
            .getOrElse { cause -> throw  ZephyrException(errorMessageTemplate.format(testCycleKey), cause) }
            .validateStatusCode { errorMessageTemplate.format(testCycleKey) }
    }
}