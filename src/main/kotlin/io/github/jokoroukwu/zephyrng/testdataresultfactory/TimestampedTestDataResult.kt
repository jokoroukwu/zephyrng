package io.github.jokoroukwu.zephyrng.testdataresultfactory

import io.github.jokoroukwu.zephyrapi.publication.DurationRange
import io.github.jokoroukwu.zephyrapi.publication.TestDataResultBase


data class TimestampedTestDataResult(
    override val startTime: Long,
    override val endTime: Long,
    val testDataResult: TestDataResultBase
) : DurationRange, Comparable<TimestampedTestDataResult> {

    override fun compareTo(other: TimestampedTestDataResult) =
        testDataResult.index.compareTo(other.testDataResult.index)

}