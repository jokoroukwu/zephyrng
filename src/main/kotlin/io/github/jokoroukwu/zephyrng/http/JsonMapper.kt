package io.github.jokoroukwu.zephyrng.http

import kotlinx.serialization.json.Json

object JsonMapper {
    val instance = Json {
        ignoreUnknownKeys = true
    }
}