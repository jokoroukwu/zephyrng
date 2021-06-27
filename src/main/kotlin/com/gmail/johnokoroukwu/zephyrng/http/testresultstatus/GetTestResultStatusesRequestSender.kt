package com.gmail.johnokoroukwu.zephyrng.http.testresultstatus

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import com.gmail.johnokoroukwu.zephyrng.config.ZephyrConfigImpl
import com.gmail.johnokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.gmail.johnokoroukwu.zephyrng.http.AbstractRequestSender
import com.gmail.johnokoroukwu.zephyrng.http.JsonMapper
import com.gmail.johnokoroukwu.zephyrng.http.ZephyrException
import com.gmail.johnokoroukwu.zephyrng.http.ZephyrResponseDeserializer
import kotlinx.serialization.json.Json

class GetTestResultStatusesRequestSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrNgConfigLoaderImpl.zephyrNgConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val urlTemplate = "$baseUrl/project/%d/testresultstatus"
    private val errorMessageTemplate = "Failed to fetch test result statuses"

    fun getTestResultStatusesRequest(
        zephyrProjectId: Int
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