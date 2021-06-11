package com.gmail.suneclips3.connectionconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ZephyrConfig(
    @SerialName("time-zone")
    private val timeZone: String,
    @SerialName("jira-url")
    private val jiraUrl: String,
    @SerialName("project-key")
    private val projectKey: String,
    private val username: String,
    private val password: String

) : IZephyrConfig {
    override fun timeZone() = timeZone

    override fun jiraUrl() = jiraUrl

    override fun projectKey() = projectKey

    override fun username() = username

    override fun password() = password
}