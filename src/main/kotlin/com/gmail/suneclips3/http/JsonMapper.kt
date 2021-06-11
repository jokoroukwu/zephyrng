package com.gmail.suneclips3.http

import kotlinx.serialization.json.Json

object JsonMapper {
    val instance: Json = Json { ignoreUnknownKeys = true }
}