package com.gmail.suneclips3.testng

import com.charleskorn.kaml.Yaml
import com.gmail.suneclips3.dto.ConnectionConfig
import java.io.FileNotFoundException

object ConnectionConfigLoader {
     fun loadCredentials(): ConnectionConfig {
        val url = ClassLoader.getSystemResource("zephyr-credentials.yml") ?: throw FileNotFoundException()
        return Yaml.default.decodeFromString(ConnectionConfig.serializer(), url.readText())
    }
}