package com.gmail.johnokoroukwu.zephyrng.http

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

val jsonMapper: Json = Json { ignoreUnknownKeys = true }

class ZephyrResponse(val statusCode: Int, val responseMessage: String, val body: ByteArray) {
    inline fun <reified T> getJsonBody() = jsonMapper.decodeFromString<T>(body.toString(Charsets.UTF_8))

    fun getStringBody() = body.toString(StandardCharsets.UTF_8)

}