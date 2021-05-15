package com.gmail.suneclips3

class TestDataEntry(private val testData: Array<Any>) {

    override fun toString() = testData.joinToString(
        prefix = "TestDataEntry: [{",
        postfix = "}]",
        separator = "}, {"
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestDataEntry
        if (!testData.contentEquals(other.testData)) return false

        return true
    }

    override fun hashCode(): Int {
        return testData.contentHashCode()
    }

}