package com.gmail.suneclips3.http.testresultstatus

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import com.gmail.suneclips3.http.AbstractRequestSender
import com.gmail.suneclips3.http.JsonMapper
import com.gmail.suneclips3.http.ZephyrException
import com.gmail.suneclips3.http.ZephyrResponseDeserializer
import kotlinx.serialization.json.Json

class GetTestResultStatusesRequestSender(
    zephyrConfig: ZephyrConfig,
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = Fuel
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val urlTemplate = "$baseUrl/project/%d/testresultstatus"
    private val errorMessageTemplate = "failed to fetch test result statuses"

    fun getTestResultStatusesRequest(
        zephyrProjectId: Int
    ): List<SerializableTestResultStatusItem> {

        return requestFactory.runCatching {
            Fuel.get(urlTemplate.format(zephyrProjectId))
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .response(ZephyrResponseDeserializer)

        }.getOrElse { cause -> throw ZephyrException(errorMessageTemplate, cause) }
            .third.get()
            .validateStatusCode { "${errorMessageTemplate}: unsuccessful status code" }
            .runCatching { getJsonBody<List<SerializableTestResultStatusItem>>() }
            .getOrElse { cause -> throw ZephyrException("${errorMessageTemplate}: deserialization error", cause) }
    }
}