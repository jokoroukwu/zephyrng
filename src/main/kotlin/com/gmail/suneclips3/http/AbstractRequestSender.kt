package com.gmail.suneclips3.http

import com.github.kittinunf.fuel.core.RequestFactory
import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import kotlinx.serialization.json.Json

const val BASE_API_URL = "/rest/tests/1.0"

abstract class AbstractRequestSender(
    protected val zephyrConfig: ZephyrConfig,
    protected val jsonMapper: Json,
    protected val requestFactory: RequestFactory.Convenience
) {
    protected val baseUrl = "${zephyrConfig.jiraUrl()}$BASE_API_URL"

    protected inline fun ZephyrResponse.validateStatusCode(validCode: Int = 200, errorMessage: () -> String) =
        if (statusCode != validCode) throw ZephyrException(errorMessage.invoke() + ": ${getStringBody()}") else this

}