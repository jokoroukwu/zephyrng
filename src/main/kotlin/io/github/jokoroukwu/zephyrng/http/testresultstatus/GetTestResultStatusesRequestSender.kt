package io.github.jokoroukwu.zephyrng.http.testresultstatus

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import io.github.jokoroukwu.zephyrng.config.ZephyrConfigImpl
import io.github.jokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import io.github.jokoroukwu.zephyrng.http.AbstractRequestSender
import io.github.jokoroukwu.zephyrng.http.JsonMapper
import io.github.jokoroukwu.zephyrng.http.ZephyrException
import io.github.jokoroukwu.zephyrng.http.ZephyrResponseDeserializer
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

        return zephyrConfig.runCatching {
            requestFactory.get(urlTemplate.format(zephyrProjectId))
                .authentication().basic(username(), password())
                .treatResponseAsValid()
                .response(ZephyrResponseDeserializer)
                .third.get()
        }.getOrElse { cause -> throw ZephyrException(errorMessageTemplate, cause) }
            .validateStatusCode { "${errorMessageTemplate}: unsuccessful status code" }
            .runCatching { getJsonBody<List<SerializableTestResultStatusItem>>() }
            .getOrElse { cause -> throw ZephyrException("${errorMessageTemplate}: deserialization error", cause) }
    }
}