package com.github.jokoroukwu.zephyrng.http.detailedreport

import com.github.jokoroukwu.zephyrng.config.ZephyrConfigImpl
import com.github.jokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.github.jokoroukwu.zephyrng.http.*
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import kotlinx.serialization.json.Json

class GetDetailedReportSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val urlTemplate = "$baseUrl/reports/testresults/detailed?projectId=%d&tql=testRun.key IN('%s')"
    private val errorMessageTemplate = "failed to fetch detailed report"

    suspend fun getDetailedReport(
        projectId: Int,
        testCycleKey: String,
    ): GetDetailedReportResponse {

        val url = urlTemplate.format(projectId, testCycleKey)
        return requestFactory.runCatching {
            get(url)
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .treatResponseAsValid()
                .await(ZephyrResponseDeserializer)

        }.getOrElse { cause -> throw ZephyrException(errorMessageTemplate + testCycleKey, cause) }
            .validateStatusCode { "$errorMessageTemplate: unsuccessful status code: {test_cycle_key: $testCycleKey}" }
            .runCatching<ZephyrResponse, GetDetailedReportResponse>(ZephyrResponse::getJsonBody)
            .getOrElse { cause ->
                throw ZephyrException(
                    "$errorMessageTemplate: body deserialization error: {test_cycle_key: $testCycleKey}",
                    cause
                )
            }
    }
}