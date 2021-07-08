package com.github.jokoroukwu.zephyrng.http

import kotlinx.serialization.decodeFromString
import java.nio.charset.StandardCharsets

class ZephyrResponse(val statusCode: Int, val responseMessage: String, val body: ByteArray) {

    inline fun <reified T> getJsonBody() =
        JsonMapper.instance.decodeFromString<T>(body.toString(Charsets.UTF_8))

    fun getStringBody() = body.toString(StandardCharsets.UTF_8)

}