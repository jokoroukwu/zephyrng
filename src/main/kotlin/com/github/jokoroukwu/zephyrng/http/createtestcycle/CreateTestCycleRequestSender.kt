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

    suspend fun createTestCycle(
        projectId: Int,
        suiteWithTestNgZephyrSuite: TestNgZephyrSuite,
    ): CreateTestCycleResponse {
        val testCycleName = suiteWithTestNgZephyrSuite.name
        return requestFactory.runCatching {
            post(url)
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .jsonBody(
                    jsonMapper.encodeToString(
                        CreateTestCycleRequest(
                            projectId = projectId,
                            name = testCycleName,
                            plannedStartDate = suiteWithTestNgZephyrSuite.plannedStartDate,
                            plannedEndDate = suiteWithTestNgZephyrSuite.plannedEndDate,
                        )
                    )
                ).treatResponseAsValid()
                .await(ZephyrResponseDeserializer)
        }
            .getOrElse { cause -> throw ZephyrException("$errorMessageTemplate $testCycleName", cause) }
            .validateStatusCode { "$errorMessageTemplate $testCycleName: unsuccessful status code" }
            .runCatching { getJsonBody<CreateTestCycleResponse>() }
            .getOrElse { cause ->
                throw ZephyrException(
                    errorMessageTemplate.format(testCycleName, "body deserialization error"), cause
                )
            }
    }
}