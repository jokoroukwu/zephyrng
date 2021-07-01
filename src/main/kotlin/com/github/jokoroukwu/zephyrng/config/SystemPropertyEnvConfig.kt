package com.github.jokoroukwu.zephyrng.config

import org.snakeyaml.engine.v2.env.EnvConfig
import java.util.*

object SystemPropertyEnvConfig : EnvConfig {
    private const val EMPTY_AS_NULL_PREFIX = ":"

    /**
     * Attempts to get system property for the provided [name] when either [environment] is null
     * or the [separator] starts with [EMPTY_AS_NULL_PREFIX],
     * which indicates that empty [environment] is not accepted
     */
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