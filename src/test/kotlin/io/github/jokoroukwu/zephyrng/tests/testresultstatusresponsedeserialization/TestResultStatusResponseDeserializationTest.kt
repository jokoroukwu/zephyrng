package io.github.jokoroukwu.zephyrng.tests.testresultstatusresponsedeserialization

import io.github.jokoroukwu.zephyrng.http.JsonMapper
import io.github.jokoroukwu.zephyrng.http.testresultstatus.SerializableTestResultStatusItem
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