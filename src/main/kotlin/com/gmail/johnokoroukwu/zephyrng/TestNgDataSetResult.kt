package com.gmail.johnokoroukwu.zephyrng

/**
 * Represents TestNG test result for a particular data set
 * of [org.testng.annotations.DataProvider]
 */
data class TestNgDataSetResult(
    val startTime: Long,
    val endTime: Long,
    val index: Int,
    val isSuccess: Boolean,
    val failedStepIndex: Int? = null,
    val failureMessage: String = ""
)