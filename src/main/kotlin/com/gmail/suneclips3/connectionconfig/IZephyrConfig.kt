package com.gmail.suneclips3.connectionconfig

interface IZephyrConfig {

    fun timeZone(): String

    fun jiraUrl(): String

    fun projectKey(): String

    fun username(): String

    fun password(): String
}