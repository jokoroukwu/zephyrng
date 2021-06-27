package com.gmail.johnokoroukwu.zephyrng.config

import java.time.ZoneId

data class ZephyrConfigImpl(
    private val timeZone: ZoneId,
    private val jiraUrl: String,
    private val projectKey: String,
    private val username: String,
    private val password: String

) : IZephyrNgConfig {
    override fun timeZone() = timeZone

    override fun jiraUrl() = jiraUrl

    override fun projectKey() = projectKey

    override fun username() = username

    override fun password() = password
}