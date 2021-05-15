package com.gmail.suneclips3.sender

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.gmail.suneclips3.dto.ConnectionConfig
import com.gmail.suneclips3.dto.internal.test_run.report.GetDetailedReportResponse

object GetTestRunDetailedReportSender {
    private const val urlTemplate = "https://%s/rest/tests/1.0/reports/testresults/detailed?projectId=%d&" +
            "scorecardOption=EXECUTION_RESULTS&tql=testRun.key IN ('%s') AND testCase.onlyLastTestResult IS false"
    private const val errorMessageTemplate = "failed to fetch detailed report of test run"

    suspend fun getDetailedReport(
        projectId: Int,
        testCycleKey: String,
        connectionConfig: ConnectionConfig
    ): GetDetailedReportResponse {

        val url = urlTemplate.format(projectId, testCycleKey)
        return kotlin.runCatching {
            Fuel.get(url)
                .authentication().basic(connectionConfig.username, connectionConfig.password)
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause -> throw ZephyrException(errorMessageTemplate + testCycleKey, cause) }
            .validateStatusCode { "$errorMessageTemplate $testCycleKey: unsuccessful status code" }
            .runCatching { getJsonBody<GetDetailedReportResponse>() }
            .getOrElse { cause ->
                throw ZephyrException("$errorMessageTemplate $testCycleKey: body deserialization error", cause)
            }
    }
}