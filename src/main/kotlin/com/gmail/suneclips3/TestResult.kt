package com.gmail.suneclips3

class TestResult(
    val index: Int,
    val hasPassed: Boolean,
    val failedStepIndex: Int? = null,
    val failureMessage: String? = null
) {

    fun hasFailedAtStep() = failedStepIndex != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestResult

        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        return index
    }

}