package com.gmail.suneclips3

data class TestNgDataSetResult(
    val startTime: Long,
    val endTime: Long,
    val index: Int,
    val hasPassed: Boolean,
    val failedStepIndex: Int? = null,
    val failureMessage: String = ""
)