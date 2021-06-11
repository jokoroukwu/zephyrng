package com.gmail.suneclips3.http.detailedreport

import kotlinx.serialization.Serializable

@Serializable
class GetDetailedReportResponse(
    val testRunsDetailReports: List<TestRunDetailReport>
)