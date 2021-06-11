package com.gmail.suneclips3.http.detailedreport

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.http.AbstractRequestSender
import com.gmail.suneclips3.http.JsonMapper
import com.gmail.suneclips3.http.ZephyrException
import com.gmail.suneclips3.http.ZephyrResponseDeserializer
import kotlinx.serialization.json.Json

class GetDetailedReportSender(
    zephyrConfig: ZephyrConfig,
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = Fuel
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val urlTemplate = "$baseUrl/reports/testresults/detailed?projectId=%d&" +
            "scorecardOption=EXECUTION_RESULTS&tql=testRun.key IN ('%s') AND testCase.onlyLastTestResult IS false"
    private val errorMessageTemplate = "failed to fetch detailed report of test cycle"

    suspend fun getDetailedReport(
        projectId: Int,
        testCycleKey: String,
        zephyrConfig: ZephyrConfig
    ): GetDetailedReportResponse {

        val url = urlTemplate.format(projectId, testCycleKey)
        return kotlin.runCatching {
            Fuel.get(url)
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause -> throw ZephyrException(errorMessageTemplate + testCycleKey, cause) }
            .validateStatusCode { "$errorMessageTemplate $testCycleKey: unsuccessful status code" }
            .runCatching { getJsonBody<GetDetailedReportResponse>() }
            .getOrElse { cause ->
                throw ZephyrException("$errorMessageTemplate $testCycleKey: body deserialization error", cause)
            }
    }
}