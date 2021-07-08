package com.github.jokoroukwu.zephyrng.http.testcycleupdate

import com.github.jokoroukwu.zephyrng.config.ZephyrConfigImpl
import com.github.jokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.github.jokoroukwu.zephyrng.http.*
import com.github.jokoroukwu.zephyrng.http.createtestcycle.CreateTestCycleResponse
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UpdateTestCycleRequestSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "${baseUrl}/testrunitem/bulk/save"
    private val errorMessageTemplate = "Failed to add test cases to Zephyr test cycle"

    /**
     * Performs and HTTP-request to populate a test cycle with test cases from [testNgZephyrSuite]
     *
     * @see [TestNgZephyrSuite]
     * @param createTestCycleResponse a DTO encapsulating created test cycle state*
     */
    suspend fun putTestCasesRequest(
        testNgZephyrSuite: TestNgZephyrSuite,
        createTestCycleResponse: CreateTestCycleResponse,
    ) {
        requestFactory.runCatching {
            put(url)
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .jsonBody(
                    jsonMapper.encodeToString(
                        UpdateTestCycleRequest(
                            testRunId = createTestCycleResponse.id,
                            addedTestRunItems = mapSuiteToTestRunItems(testNgZephyrSuite)
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
        { i, zephyrTestResult -> TestRunItem(i, UpdateTestCycleTestResult(zephyrTestResult.id)) }
    }
}