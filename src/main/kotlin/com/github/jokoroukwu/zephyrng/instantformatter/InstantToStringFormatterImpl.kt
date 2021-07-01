package com.github.jokoroukwu.zephyrng.instantformatter

import com.github.jokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class InstantToStringFormatterImpl(
    private val zoneId: ZoneId = ZephyrNgConfigLoaderImpl.zephyrNgConfig().timeZone(),
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
) : InstantToStringFormatter {

    override fun formatInstant(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis).atZone(zoneId).format(dateTimeFormatter)
    }

    override fun formatInstant(instant: Instant): String {
        return instant.atZone(zoneId).format(dateTimeFormatter)
    }
}