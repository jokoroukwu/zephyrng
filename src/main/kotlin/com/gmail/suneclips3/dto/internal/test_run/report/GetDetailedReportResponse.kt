package com.gmail.suneclips3.dto.internal.test_run.report

import kotlinx.serialization.Serializable

@Serializable
class GetDetailedReportResponse(
    val testRunDetailedReports: List<TestRunDetailReport>
)