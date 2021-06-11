package com.gmail.suneclips3.tests.testrundetailedreportresponseparser

import com.gmail.suneclips3.http.JsonMapper
import com.gmail.suneclips3.http.detailedreport.GetDetailedReportResponse
import kotlinx.serialization.decodeFromString
import org.assertj.core.api.Assertions
import org.testng.annotations.Test
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets

class GetDetailedReportResponseParseTest {
    private val fileName = "detailed-report.json"

    @Test
    fun `should not throw exception when deserializing`() {
        val url = ClassLoader.getSystemResource(fileName) ?: throw FileNotFoundException(fileName)
        val jsonString = url.readText(StandardCharsets.UTF_8)

        Assertions.assertThatCode { JsonMapper.instance.decodeFromString<GetDetailedReportResponse>(jsonString) }
            .`as`("json deserialization")
            .doesNotThrowAnyException()
    }
}