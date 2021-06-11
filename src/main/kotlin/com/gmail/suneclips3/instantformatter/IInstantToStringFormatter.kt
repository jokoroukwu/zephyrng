package com.gmail.suneclips3.instantformatter

import java.time.Instant

interface IInstantToStringFormatter {

    fun formatInstant(epochMillis: Long): String

    fun formatInstant(instant: Instant): String
}