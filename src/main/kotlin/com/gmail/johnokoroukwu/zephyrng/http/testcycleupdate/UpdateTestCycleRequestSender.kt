package com.gmail.johnokoroukwu.zephyrng.http.testcycleupdate

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.gmail.johnokoroukwu.zephyrng.config.ZephyrConfigImpl
import com.gmail.johnokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.gmail.johnokoroukwu.zephyrng.http.*
import com.gmail.johnokoroukwu.zephyrng.http.createtestcycle.CreateTestCycleResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UpdateTestCycleRequestSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "${baseUrl}/testrunitem/bulk/save"
    private val errorMessageTemplate = "Failed to add test cases to Zephyr test cycle"

    suspend fun putTestCasesRequest(
        suite: TestNgZephyrSuite,
        createTestCycleResponse: CreateTestCycleResponse,
    ) {
        requestFactory.runCatching {
            put(url)
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .jsonBody(
                    jsonMapper.encodeToString(
                        UpdateTestCycleRequest(
                            testRunId = createTestCycleResponse.id,
                            addedTestRunItems = mapSuiteToTestRunItems(suite)
                        )
                    )
                )
                .treatResponseAsValid()
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause -> throw ZephyrException("$errorMessageTemplate '${createTestCycleResponse.key}'", cause) }
            .validateStatusCode { "$errorMessageTemplate '${createTestCycleResponse.key}': unsuccessful status code" }
    }

    private fun mapSuiteToTestRunItems(suite: TestNgZephyrSuite): List<TestRunItem> {
        val testCasesWithResults = suite.testCasesWithDataSetResults
        return testCasesWithResults.mapIndexedTo(ArrayList(testCasesWithResults.size))
        { i, zephyrTestResult -> TestRunItem(i, SerializableTestResult(zephyrTestResult.id)) }
    }
}