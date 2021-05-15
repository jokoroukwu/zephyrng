package com.gmail.suneclips3.sender

import kotlinx.serialization.decodeFromString


fun ZephyrResponse.validateStatusCode(validCode: Int = 200, errorMessage: () -> String) =
    if (statusCode != validCode) throw ZephyrException(errorMessage()) else this

class ZephyrResponse(val statusCode: Int, val body: ByteArray) {

    inline fun <reified T> getJsonBody() = ZephyrJson.instance.decodeFromString<T>(body.toString(Charsets.UTF_8))

}