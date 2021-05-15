package com.gmail.suneclips3.sender

import kotlinx.serialization.json.Json

object ZephyrJson {
    val instance: Json = Json { ignoreUnknownKeys = true }
}