package com.gmail.johnokoroukwu.zephyrng.config

import java.time.ZoneId

interface IZephyrNgConfig {

    fun timeZone(): ZoneId

    fun jiraUrl(): String

    fun projectKey(): String

    fun username(): String

    fun password(): String
}