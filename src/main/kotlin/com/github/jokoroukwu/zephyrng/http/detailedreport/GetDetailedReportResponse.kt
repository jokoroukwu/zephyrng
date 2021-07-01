package com.github.jokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class GetDetailedReportResponse(
    val testRunsDetailReports: List<TestRunDetailReport>
)