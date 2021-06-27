package com.gmail.johnokoroukwu.zephyrng.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class GetDetailedReportResponse(
    val testRunsDetailReports: List<TestRunDetailReport>
)