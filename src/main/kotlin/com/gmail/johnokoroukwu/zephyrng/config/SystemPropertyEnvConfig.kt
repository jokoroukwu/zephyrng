package com.gmail.johnokoroukwu.zephyrng.config

import org.snakeyaml.engine.v2.env.EnvConfig
import java.util.*

object SystemPropertyEnvConfig : EnvConfig {
    private const val EMPTY_AS_NULL_PREFIX = ":"

    override fun getValueFor(
        name: String,
        separator: String,
        value: String,
        environment: String?
    ): Optional<String> {
        return when {
            environment == null -> tryGetProperty(name)
            environment.isEmpty() && separator.startsWith(EMPTY_AS_NULL_PREFIX) -> tryGetProperty(name)
            else -> Optional.of(environment)
        }
    }

    private fun tryGetProperty(name: String) = Optional.ofNullable(System.getProperty(name))
}