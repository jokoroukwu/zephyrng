package com.github.jokoroukwu.zephyrng.config

import java.time.ZoneId

interface IZephyrNgConfig {
    /**
     * The actual timezone used to display Zephyr test result
     * start and end time
     */
    fun timeZone(): ZoneId

    /**
     * JIRA server URL e.g https://my-jira:9091
     */
    fun jiraUrl(): String

    /**
     * JIRA project key
     */
    fun projectKey(): String

    /**
     * JIRA login
     */
    fun username(): String

    /**
     * JIRA password
     */
    fun password(): String
}