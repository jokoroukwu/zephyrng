package com.gmail.suneclips3.tests.updatableresultmapper

import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.http.detailedreport.GetDetailedReportSender
import com.gmail.suneclips3.http.testresultstatus.TestResultStatus
import io.mockk.clearMocks
import io.mockk.mockk
import org.testng.annotations.AfterMethod
import java.util.*

class UpdatableResultMapperTest {
    private val testResultStatusToIdMap = EnumMap<TestResultStatus, Int>(TestResultStatus::class.java).apply {
        put(TestResultStatus.BLOCKED, 1)
        put(TestResultStatus.FAIL, 2)
        put(TestResultStatus.IN_PROGRESS, 3)
        put(TestResultStatus.NOT_EXECUTED, 4)
        put(TestResultStatus.PASS, 5)
    }
    private val getDetailedReportSender: GetDetailedReportSender = mockk()
    private val zephyrConfig: ZephyrConfig = ZephyrConfig(
        "", "", "", "", ""
    )
    private val projectId = 1

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearMocks(getDetailedReportSender)
    }

  //  @Test
    fun testName() {
        /* runBlocking {
             UpdatableResultsMapper(connectionConfig, getDetailedReportSender)
                 .mapToUpdatableResults(projectId,testResultStatusToIdMap,)
         }*/
    }
}