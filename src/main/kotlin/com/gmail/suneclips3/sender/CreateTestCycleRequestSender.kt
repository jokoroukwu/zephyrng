package com.gmail.suneclips3.sender

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.gmail.suneclips3.ZephyrTestCycle
import com.gmail.suneclips3.dto.ConnectionConfig
import com.gmail.suneclips3.dto.internal.test_run.CreateTestRunRequest
import com.gmail.suneclips3.dto.internal.test_run.CreateTestCycleResponse
import kotlinx.serialization.encodeToString
import java.time.Instant

object CreateTestCycleRequestSender {
    private const val createTestRunRequestURL = "https://%s/rest/tests/1.0/testrun"
    private const val errorMessageTemplate = "failed to create Zephyr test cycle %s {reason: %s}"

    suspend fun createTestCycle(
        zephyrTestCycle: ZephyrTestCycle,
        credentials: ConnectionConfig
    ): CreateTestCycleResponse {

        val testCycleName = zephyrTestCycle.name
        return kotlin.runCatching {
            val now = Instant.now().toString()
            Fuel.post(createTestRunRequestURL)
                .authentication().basic(credentials.username, credentials.password)
                .body(
                    ZephyrJson.instance.encodeToString(
                        CreateTestRunRequest(
                            projectId = zephyrTestCycle.zephyrProjectId,
                            name = testCycleName,
                            statusId = 1,
                            plannedStartDate = now,
                            plannedEndDate = now,
                        )
                    )
                ).await(ZephyrResponseDeserializer)
        }
            .getOrElse { cause -> throw ZephyrException(errorMessageTemplate.format(testCycleName, cause)) }
            .validateStatusCode { errorMessageTemplate.format(testCycleName, "unsuccessful status code") }
            .runCatching { getJsonBody<CreateTestCycleResponse>() }
            .getOrElse { cause ->
                throw ZephyrException(
                    errorMessageTemplate.format(testCycleName, "body deserialization error"), cause
                )
            }
    }
}