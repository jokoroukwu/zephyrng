package com.gmail.suneclips3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionConfig(
     val username: String,
     val password: String,
    @SerialName("project-key")
     val projectKey: String,
    @SerialName("jira-url")
     val jiraUrl: String
) {
}