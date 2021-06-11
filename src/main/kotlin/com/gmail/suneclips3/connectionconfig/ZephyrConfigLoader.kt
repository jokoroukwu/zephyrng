package com.gmail.suneclips3.connectionconfig

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths

object ZephyrConfigLoader : IZephyrConfigLoader {
    private const val classpathFileName = "testng-zephyr-config.yml"
    private const val systemPropName = "testng.zephyr.config"
    private const val systemEnvVar = "TESTNG_ZEPHYR_CONFIG"
    private val ZEPHYR_CONFIG: ZephyrConfig = loadConfig()

    override fun connectionConfig() = ZEPHYR_CONFIG

    private fun loadConfig(): ZephyrConfig {
        val json = System.getenv(systemEnvVar)?.readFile()
            ?: System.getProperty(systemPropName)?.readFile()
            ?: ClassLoader.getSystemResource(classpathFileName)?.readText()
            ?: throw FileNotFoundException("Zephyr connection config file not found at classpath: $classpathFileName")

        return Yaml.default.decodeFromString(json)
    }

    private fun String.readFile(): String {
        return Paths.get(this).takeIf(Files::exists)?.run(Files::readString)
            ?: throw  FileNotFoundException("Zephyr config file not found: $this")
    }

}