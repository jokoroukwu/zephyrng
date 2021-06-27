package com.gmail.johnokoroukwu.zephyrng.tests.testresultstatusresponsedeserialization

import com.gmail.johnokoroukwu.zephyrng.http.JsonMapper
import com.gmail.johnokoroukwu.zephyrng.http.testresultstatus.SerializableTestResultStatusItem
import kotlinx.serialization.decodeFromString
import org.assertj.core.api.Assertions
import org.testng.annotations.Test
import java.io.FileNotFoundException

class TestResultStatusResponseDeserializationTest {
    private val fileName = "json/test-result-status-response.json"

    @Test
    fun `should not throw deserialization exception`() {
        val json = ClassLoader.getSystemResource(fileName)?.readText() ?: throw FileNotFoundException(fileName)
        println(json)
        Assertions.assertThatCode { JsonMapper.instance.decodeFromString<List<SerializableTestResultStatusItem>>(json) }
            .doesNotThrowAnyException()
    }
}