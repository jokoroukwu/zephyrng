package com.gmail.johnokoroukwu.zephyrng.http

import kotlinx.serialization.json.Json

object JsonMapper {
    val instance = Json { ignoreUnknownKeys = true }
}