package com.gmail.suneclips3.http.testscriptresult

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.http.AbstractRequestSender
import com.gmail.suneclips3.http.JsonMapper
import com.gmail.suneclips3.http.ZephyrException
import com.gmail.suneclips3.http.ZephyrResponseDeserializer
import com.gmail.suneclips3.http.detailedreport.TestScriptResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UpdateTestScriptResultsRequestSender(
    zephyrConfig: ZephyrConfig,
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = Fuel
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "$baseUrl/testscriptresult"
    private val errorMessageTemplate = "Failed to update test script results of test cycle"

    suspend fun updateTestScriptResults(testCycleKey: String, testScriptResults: Collection<TestScriptResult>) {
        println("updating test script results: {test_cycle_key: $testCycleKey")
        requestFactory.runCatching {
            put(url).authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .jsonBody(jsonMapper.encodeToString(testScriptResults))
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause ->
            throw  ZephyrException("$errorMessageTemplate '$testCycleKey'", cause)
        }.validateStatusCode { "$errorMessageTemplate '$testCycleKey'" }
    }
}