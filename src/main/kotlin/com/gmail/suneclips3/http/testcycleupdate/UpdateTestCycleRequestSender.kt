package com.gmail.suneclips3.http.testcycleupdate

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.http.*
import com.gmail.suneclips3.http.createtestcycle.CreateTestCycleResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UpdateTestCycleRequestSender(
    zephyrConfig: ZephyrConfig,
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = Fuel
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "${baseUrl}testrunitem/bulk/save"
    private val errorMessageTemplate = "failed to add test cases to Zephyr test cycle"

    suspend fun putTestCasesRequest(
        suite: TestSuiteWithTestCaseResults,
        createTestCycleResponse: CreateTestCycleResponse,
        zephyrConfig: ZephyrConfig,
    ) {
        requestFactory.runCatching {
            get(url)
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .jsonBody(
                    JsonMapper.instance.encodeToString(
                        UpdateTestCycleRequest(
                            testRunId = createTestCycleResponse.id,
                            addedSerializableTestRunItems = mapSuiteToTestRunItems(suite)
                        )
                    )
                )
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause -> throw ZephyrException("$errorMessageTemplate '${createTestCycleResponse.key}'", cause) }
            .validateStatusCode { "$errorMessageTemplate '${createTestCycleResponse.key}': unsuccessful status code" }
    }

    private fun mapSuiteToTestRunItems(suite: TestSuiteWithTestCaseResults): List<SerializableTestRunItem> {
        val testCasesWithResults = suite.testCasesWithDataSetResults
        return testCasesWithResults.mapIndexedTo(ArrayList(testCasesWithResults.size))
        { i, zephyrTestResult -> SerializableTestRunItem(i, SerializableTestResult(zephyrTestResult.id)) }
    }
}