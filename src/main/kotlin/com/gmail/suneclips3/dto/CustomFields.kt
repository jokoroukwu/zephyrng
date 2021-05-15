package com.gmail.suneclips3.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomFields(
    @SerialName("Build Number")
    val buildNumber: Int? = null,
    @SerialName("Category")
    val category: List<String>? = null,
    @SerialName("Implemented")
    val implemented: Boolean? = null,
    @SerialName("Pre-Condition(s)")
    val preConditions: String? = null,
    @SerialName("Release Date")
    val releaseDate: String? = null,
    @SerialName("Tester")
    val tester: String? = null,
)