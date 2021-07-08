package com.github.jokoroukwu.zephyrng.http.createtestcycle

import com.github.jokoroukwu.zephyrng.config.ZephyrConfigImpl
import com.github.jokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.github.jokoroukwu.zephyrng.http.*
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CreateTestCycleRequestSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory,
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "$baseUrl/testrun"
    private val errorMessageTemplate = "failed to create Zephyr test cycle"

    /**
     * Performs an HTTP request to create a new Zephyr test cycle.
     * The provided [testNgZephyrSuite] name will be joined with start and end time properties
     * and used as test cycle name.
     *
     * @param projectId JIRA project id
     * @param testNgZephyrSuite suite
     */
    suspend fun createTestCycle(
        projectId: Long,
        testNgZephyrSuite: TestNgZephyrSuite,
    ): CreateTestCycleResponse {
        val testCycleName = testNgZephyrSuite.name
        return zephyrConfig.runCatching {
            requestFactory.post(url)
                .authentication().basic(username(), password())
                .jsonBody(
                    jsonMapper.encodeToString(
                        CreateTestCycleRequest(
                            projectId = projectId,
                            name = testCycleName,
                            plannedStartDate = testNgZephyrSuite.plannedStartDate,
                            plannedEndDate = testNgZephyrSuite.plannedEndDate,
                        )
                    )
                ).treatResponseAsValid()
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause -> throw ZephyrException("$errorMessageTemplate $testCycleName", cause) }
            .validateStatusCode { "$errorMessageTemplate $testCycleName: unsuccessful status code" }
            .runCatching { getJsonBody<CreateTestCycleResponse>() }
            .getOrElse { cause ->
                throw ZephyrException(
                    errorMessageTemplate.format(testCycleName, "body deserialization error"), cause
                )
            }
    }
}