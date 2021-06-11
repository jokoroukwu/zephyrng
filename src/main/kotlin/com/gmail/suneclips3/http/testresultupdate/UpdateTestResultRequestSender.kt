package com.gmail.suneclips3.http.testresultupdate

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.gmail.suneclips3.UpdatableTestResult
import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.http.AbstractRequestSender
import com.gmail.suneclips3.http.JsonMapper
import com.gmail.suneclips3.http.ZephyrException
import com.gmail.suneclips3.http.ZephyrResponseDeserializer
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UpdateTestResultRequestSender(
    zephyrConfig: ZephyrConfig,
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = Fuel,
    private val commentRowsFormatter: ICommentRowFormatter = CommentRowsFormatter
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "$baseUrl/testresult"

    suspend fun updateTestResult(
        testCycleKey: String,
        statusToIdMap: Map<TestResultStatus, Int>,
        testResults: Collection<UpdatableTestResult>
    ) {
        println("updating test results of test cycle '$testCycleKey'")
        requestFactory.runCatching {
            put(url)
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .jsonBody(
                    jsonMapper.encodeToString(
                        mapToSerializableTestResults(
                            statusToIdMap,
                            testResults
                        )
                    )
                )
                .await(ZephyrResponseDeserializer)

        }.getOrElse { cause ->
            throw ZephyrException("failed to update test results of test run '$testCycleKey'", cause)
        }.validateStatusCode { "Failed to update test results of test run '$testCycleKey': unsuccessful status code" }
        println("successfully updated results of test cycle '$testCycleKey'")
    }


    private fun mapToSerializableTestResults(
        testResultStatusToIdMap: Map<TestResultStatus, Int>,
        updatableTestResults: Collection<UpdatableTestResult>
    ): List<SerializableTestResult> {

        val serializableTestResults = ArrayList<SerializableTestResult>(updatableTestResults.size)
        for (updatableTestResult in updatableTestResults) {
            val testResultStatus = updatableTestResult.getEffectiveStatus();
            val testResultStatusId = testResultStatusToIdMap.getValue(testResultStatus)
            serializableTestResults.add(
                SerializableTestResult(
                    id = updatableTestResult.testResultId,
                    testResultStatusId = testResultStatusId,
                    comment = commentRowsFormatter.formatCommentRows(updatableTestResult.commentRows)
                )
            )
        }
        return serializableTestResults
    }
}
