package io.github.jokoroukwu.zephyrng.http.testresultupdate

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import io.github.jokoroukwu.zephyrng.config.ZephyrConfigImpl
import io.github.jokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import io.github.jokoroukwu.zephyrng.http.AbstractRequestSender
import io.github.jokoroukwu.zephyrng.http.JsonMapper
import io.github.jokoroukwu.zephyrng.http.ZephyrException
import io.github.jokoroukwu.zephyrng.http.ZephyrResponseDeserializer
import io.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import io.github.jokoroukwu.zephyrng.updatableresult.UpdatableTestResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.max

class TestResultUpdater(
    zephyrConfig: ZephyrConfigImpl = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory,
    private val commentRowsFormatter: ICommentRowFormatter = CommentRowColorFormatter
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "$baseUrl/testresult"

    suspend fun updateTestResult(
        testCycleKey: String,
        statusToIdMap: Map<TestResultStatus, Long>,
        testResults: Collection<UpdatableTestResult>
    ) {
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
                .treatResponseAsValid()
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause ->
            throw ZephyrException("Failed to update test results of test run '$testCycleKey'", cause)
        }.validateStatusCode { "Failed to update test results of test run '$testCycleKey': unsuccessful status code" }
    }


    private fun mapToSerializableTestResults(
        testResultStatusToIdMap: Map<TestResultStatus, Long>,
        updatableTestResults: Collection<UpdatableTestResult>
    ): List<SerializableTestResult> {

        val serializableTestResults = ArrayList<SerializableTestResult>(updatableTestResults.size)
        for (updatableTestResult in updatableTestResults) {
            val testResultStatus = updatableTestResult.effectiveStatus
            val testResultStatusId = testResultStatusToIdMap.getValue(testResultStatus)
            serializableTestResults.add(
                SerializableTestResult(
                    id = updatableTestResult.testResultId,
                    testResultStatusId = testResultStatusId,
                    comment = commentRowsFormatter.formatCommentRow(updatableTestResult.commentRows),
                    executionTime = max(updatableTestResult.endTime - updatableTestResult.startTime, 0)
                )
            )
        }
        return serializableTestResults
    }
}
