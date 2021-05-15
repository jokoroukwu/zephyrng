package com.gmail.suneclips3.sender

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.gmail.suneclips3.ZephyrTestCycle
import com.gmail.suneclips3.dto.ConnectionConfig
import com.gmail.suneclips3.dto.internal.test_run.CreateTestCycleResponse
import com.gmail.suneclips3.dto.internal.test_run.update_test_run.SerializableTestResult
import com.gmail.suneclips3.dto.internal.test_run.update_test_run.SerializableTestRunItem
import com.gmail.suneclips3.dto.internal.test_run.update_test_run.UpdateTestCycleRequest
import kotlinx.serialization.encodeToString

object PutTestCasesRequestSender {
    private const val urlTemplate = "%s/rest/tests/1.0/testrunitem/bulk/save"
    private const val errorMessageTemplate = "failed to add test cases to Zephyr test cycle"

    suspend fun putTestCasesRequest(
        zephyrTestCycle: ZephyrTestCycle,
        createTestCycleResponse: CreateTestCycleResponse,
        connectionConfig: ConnectionConfig,
    ) {
        kotlin.runCatching {
            Fuel.get(urlTemplate.format(connectionConfig.jiraUrl))
                .authentication().basic(connectionConfig.username, connectionConfig.password)
                .jsonBody(
                    ZephyrJson.instance.encodeToString(
                        UpdateTestCycleRequest(
                            createTestCycleResponse.id,
                            zephyrTestCycleToTestRunItems(zephyrTestCycle)
                        )
                    )
                )
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause -> throw ZephyrException(errorMessageTemplate + createTestCycleResponse.key, cause) }
            .validateStatusCode { "$errorMessageTemplate ${createTestCycleResponse.key}: unsuccessful status code" }
    }

    private fun zephyrTestCycleToTestRunItems(zephyrTestCycle: ZephyrTestCycle): List<SerializableTestRunItem> {
        val zephyrTestResults = zephyrTestCycle.zephyrTestResults
        return zephyrTestResults.mapIndexedTo(ArrayList(zephyrTestResults.size))
        { i, zephyrTestResult -> SerializableTestRunItem(i, SerializableTestResult(zephyrTestResult.testCaseId)) }
    }
}