package com.gmail.suneclips3.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Owner(
    val accountId: String?,
    val self: String?
)