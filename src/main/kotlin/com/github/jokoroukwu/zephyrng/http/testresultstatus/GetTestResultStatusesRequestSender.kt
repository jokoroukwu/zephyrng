package com.github.jokoroukwu.zephyrng.http.testresultstatus

import com.github.jokoroukwu.zephyrng.config.ZephyrConfigImpl
import com.github.jokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.github.jokoroukwu.zephyrng.http.AbstractRequestSender
import com.github.jokoroukwu.zephyrng.http.JsonMapper
import com.github.jokoroukwu.zephyrng.http.ZephyrException
import com.github.jokoroukwu.zephyrng.http.ZephyrResponseDeserializer
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import kotlinx.serialization.json.Json

class GetTestResultStatusesRequestSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val urlTemplate = "$baseUrl/project/%d/testresultstatus"
    private val errorMessageTemplate = "Failed to fetch test result statuses"

    fun getTestResultStatusesRequest(
        zephyrProjectId: Long
    ): List<SerializableTestResultStatusItem> {

        return requestFactory.runCatching {
            get(urlTemplate.format(zephyrProjectId))
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .treatResponseAsValid()
                .response(ZephyrResponseDeserializer)
                .third.get()

        }.getOrElse { cause -> throw ZephyrException(errorMessageTemplate, cause) }
            .validateStatusCode { "${errorMessageTemplate}: unsuccessful status code" }
            .runCatching { getJsonBody<List<SerializableTestResultStatusItem>>() }
            .getOrElse { cause -> throw ZephyrException("${errorMessageTemplate}: deserialization error", cause) }
    }
}