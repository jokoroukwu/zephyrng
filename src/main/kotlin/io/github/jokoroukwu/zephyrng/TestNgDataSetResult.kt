package io.github.jokoroukwu.zephyrng

/**
 * Represents TestNG test result for a particular data set.
 * Used primarily to separate data driven tests results, although
 * non data driven test will still be associated with a single [TestNgDataSetResult].
 *
 * @see [org.testng.annotations.DataProvider]
 */
data class TestNgDataSetResult(
    val startTime: Long,
    val endTime: Long,
    val index: Int,
    val isSuccess: Boolean,
    val failedStepIndex: Int? = null,
    val failureMessage: String = ""
)