package com.github.jokoroukwu.zephyrng.instantformatter

import java.time.Instant

interface InstantToStringFormatter {

    fun formatInstant(epochMillis: Long): String

    fun formatInstant(instant: Instant): String
}