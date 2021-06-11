package com.gmail.suneclips3.instantformatter

import com.gmail.suneclips3.connectionconfig.ZephyrConfigLoader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class InstantToStringFormatter(
    private val zoneId: ZoneId = ZoneId.of(ZephyrConfigLoader.connectionConfig().timeZone()),
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
) : IInstantToStringFormatter {

    override fun formatInstant(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis).atZone(zoneId).format(dateTimeFormatter)
    }

    override fun formatInstant(instant: Instant): String {
        return instant.atZone(zoneId).format(dateTimeFormatter)
    }
}