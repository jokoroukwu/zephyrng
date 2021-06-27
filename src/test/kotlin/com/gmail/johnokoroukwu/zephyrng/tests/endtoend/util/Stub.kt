package com.gmail.johnokoroukwu.zephyrng.tests.endtoend.util

import com.github.kittinunf.fuel.core.Headers
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.gmail.johnokoroukwu.zephyrng.config.IZephyrNgConfig
import com.gmail.johnokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.gmail.johnokoroukwu.zephyrng.http.AbstractRequestSender.Companion.BASE_API_URL
import com.gmail.johnokoroukwu.zephyrng.http.createtestcycle.CreateTestCycleResponse
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.GetDetailedReportResponse
import com.gmail.johnokoroukwu.zephyrng.http.detailedreport.TestRunDetailReport
import com.gmail.johnokoroukwu.zephyrng.http.gettestcases.GetTestCasesResponse
import com.gmail.johnokoroukwu.zephyrng.http.gettestcases.MAX_RESULTS
import com.gmail.johnokoroukwu.zephyrng.http.gettestcases.ResultItem
import com.gmail.johnokoroukwu.zephyrng.http.testresultstatus.SerializableTestResultStatusItem
import com.gmail.johnokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.gmail.johnokoroukwu.zephyrng.tests.endtoend.util.CustomRequestMatcher.urlStartsWith
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.http.entity.ContentType
import java.util.*

const val DEFAULT_CYCLE_KEY = "test-cycle-key"
const val DEFAULT_PROJECT_ID = 1
const val DEFAULT_CYCLE_ID = 1

fun WireMockServer.stubUpdateTestScriptResultsRequest(
    config: IZephyrNgConfig = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    response: ResponseDefinitionBuilder = ok().applyResponseHeaders()
): UUID = stubFor(
    put("$BASE_API_URL/testscriptresult")
        .withBasicAuth(config.username(), config.password())
        .willReturn(response)
).id


fun WireMockServer.stubUpdateTestResultsRequest(config: IZephyrNgConfig = ZephyrNgConfigLoaderImpl.zephyrNgConfig()): UUID =
    stubFor(
        put("$BASE_API_URL/testresult")
            .withBasicAuth(config.username(), config.password())
            .willReturn(ok().applyResponseHeaders())
    ).id


fun WireMockServer.stubGetDetailedReportRequest(
    testRunDetailReport: TestRunDetailReport,
    cycleKey: String = DEFAULT_CYCLE_KEY,
    projectId: Int = DEFAULT_PROJECT_ID,
    config: IZephyrNgConfig = ZephyrNgConfigLoaderImpl.zephyrNgConfig()
): UUID {
    return stubFor(
        get(
            "$BASE_API_URL/reports/testresults/detailed?projectId=$projectId&tql=testRun.key%20IN('$cycleKey')"
        )
            .withBasicAuth(config.username(), config.password())
            .willReturn(
                ok().applyResponseHeaders().withBody(
                    Json.encodeToString(
                        GetDetailedReportResponse(
                            listOf(testRunDetailReport)
                        )
                    )
                )
            )
    ).id
}


fun WireMockServer.stubUpdateTestCycleRequest(
    config: IZephyrNgConfig = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    response: ResponseDefinitionBuilder = ok().applyResponseHeaders()
): UUID = stubFor(
    put("$BASE_API_URL/testrunitem/bulk/save")
        .withBasicAuth(config.username(), config.password())
        .willReturn(response)
).id


fun WireMockServer.stubCreateTestCycleRequest(
    config: IZephyrNgConfig = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    responseBody: () -> CreateTestCycleResponse = {
        CreateTestCycleResponse(id = DEFAULT_CYCLE_ID, key = DEFAULT_CYCLE_KEY)
    }

): UUID {
    return stubFor(
        post("$BASE_API_URL/testrun")
            .withBasicAuth(config.username(), config.password())
            .willReturn(ok().applyResponseHeaders().withBody(Json.encodeToString(responseBody())))
    ).id

}


fun WireMockServer.stubGetTestResultStatusesRequest(
    projectId: Int = DEFAULT_PROJECT_ID,
    config: IZephyrNgConfig = ZephyrNgConfigLoaderImpl.zephyrNgConfig()
) {
    stubFor(
        get(urlEqualTo("$BASE_API_URL/project/$projectId/testresultstatus"))
            .withBasicAuth(config.username(), config.password())
            .willReturn(
                ok().applyResponseHeaders().withBody(
                    with(EnumSet.allOf(TestResultStatus::class.java)) {
                        ArrayList<SerializableTestResultStatusItem>().also {
                            forEachIndexed { i, status -> it.add(SerializableTestResultStatusItem(i, status)) }
                        }
                    }.run(Json::encodeToString)
                )
            )
    )
}


fun WireMockServer.stubGetTestCasesRequest(
    resultItems: List<ResultItem> = listOf(
        ResultItem(id = 1, key = DATA_DRIVEN_TEST_CASE_KEY, projectId = DEFAULT_PROJECT_ID),
        ResultItem(id = 2, key = NON_DATA_DRIVEN_TEST_CASE, projectId = DEFAULT_PROJECT_ID)
    ),
    getTesCaseResponseBuilder: () -> GetTestCasesResponse = {
        GetTestCasesResponse(
            results = resultItems,
            total = resultItems.size,
            startAt = 0,
            maxResults = MAX_RESULTS
        )
    },
    zephyrNgConfig: IZephyrNgConfig = ZephyrNgConfigLoaderImpl.zephyrNgConfig()
) {
    val url = "$BASE_API_URL/testcase/search?fields=id,key,projectId&maxResults=$MAX_RESULTS&query=testCase.key%20IN"
    stubFor(
        get(anyUrl())
            .withBasicAuth(zephyrNgConfig.username(), zephyrNgConfig.password())
            .andMatching(urlStartsWith(url))
            .willReturn(ok().applyResponseHeaders().withBody(Json.encodeToString(getTesCaseResponseBuilder())))
    )
}


private fun ResponseDefinitionBuilder.applyResponseHeaders() =
    withHeader(Headers.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
        .withHeader("Connection", "keep-alive")