package com.gmail.suneclips3.http.createtestcycle

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.connectionconfig.ZephyrConfigLoader
import com.gmail.suneclips3.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

class CreateTestCycleRequestSender(
    config: ZephyrConfig = ZephyrConfigLoader.connectionConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = Fuel
) : AbstractRequestSender(config, jsonMapper, requestFactory) {

    private val url = "$baseUrl/testrun"
    private val errorMessageTemplate = "failed to create Zephyr test cycle"

    suspend fun createTestCycle(
        projectId: Int,
        suiteWithTestCaseResults: TestSuiteWithTestCaseResults,
        credentials: ZephyrConfig
    ): CreateTestCycleResponse {

        val testCycleName = suiteWithTestCaseResults.name
        return kotlin.runCatching {
            val nowUTC = Instant.now().toString()
            Fuel.post(url)
                .authentication().basic(credentials.username(), credentials.password())
                .body(
                    JsonMapper.instance.encodeToString(
                        SerializableCreateTestCycleRequest(
                            projectId = projectId,
                            name = testCycleName,
                            plannedStartDate = nowUTC,
                            plannedEndDate = nowUTC,
                        )
                    )
                ).await(ZephyrResponseDeserializer)
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