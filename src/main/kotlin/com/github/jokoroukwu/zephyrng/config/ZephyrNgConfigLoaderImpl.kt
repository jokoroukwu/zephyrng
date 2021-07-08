package com.github.jokoroukwu.zephyrng.config

import mu.KotlinLogging
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZoneId
import java.util.*

private val logger = KotlinLogging.logger { }

object ZephyrNgConfigLoaderImpl : ZephyrNgConfigLoader {
    private const val fileName = "zephyrng-config.yml"
    private const val systemPropName = "zephyrng.config"
    private const val envVarName = "ZEPHYRNG_CONFIG"
    private val config: ZephyrConfigImpl = loadConfig()

    override fun zephyrNgConfig() = config

    @Suppress("UNCHECKED_CAST")
    private fun loadConfig(): ZephyrConfigImpl {
        val yamlInputStream = System.getenv(envVarName)?.fileInputStream()
            ?: System.getProperty(systemPropName)?.fileInputStream()
            ?: ClassLoader.getSystemResourceAsStream(fileName)
            ?: throw FileNotFoundException("ZephyrNG configuration file not found: $fileName")

        val settings = LoadSettings.builder()
            .setEnvConfig(Optional.of(SystemPropertyEnvConfig))
            .setScalarResolver(JsonScalarResolver().also {
                it.addImplicitResolver(Tag.ENV_TAG, ENV_OR_PROP_FORMAT, "$")
            })
            .setDefaultMap { i -> HashMap<Any?, Any?>(i, 1F) }
            .build()

        return yamlInputStream.use {
            Load(settings, SystemPropResolvingConstructor(settings)).loadFromInputStream(it.buffered())
                .let { yamlObject -> yamlObject as Map<String, Any?> }
                .run(ZephyrNgConfigLoaderImpl::parseZephyrConfig)
                .also { logger.debug { "ZephyrNg configuration loaded successfully" } }
        }
    }

    private fun parseZephyrConfig(propertyMap: Map<String, Any?>) =
        with(propertyMap) {
            ZephyrConfigImpl(
                timeZone = ZoneId.of(getProperty("time-zone")),
                projectKey = getProperty("project-key"),
                jiraUrl = getProperty("jira-url"),
                username = getProperty("username"),
                password = getProperty("password")
            )
        }

    private fun Map<String, Any?>.getProperty(key: String) =
        get(key) as? String ?: throw NoSuchElementException("Missing mandatory configuration property: $key")

    private fun String.fileInputStream(): InputStream {
        return Paths.get(this).takeIf(Files::exists)?.run(Files::newInputStream)
            ?.also { logger.trace { "ZephyrNG configuration file found: $this" } }
            ?: throw  FileNotFoundException("ZephyrNG configuration file not found: $this")
    }
}

