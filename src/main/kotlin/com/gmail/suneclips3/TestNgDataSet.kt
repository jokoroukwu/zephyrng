package com.gmail.suneclips3

 class TestNgDataSet(private val testData: Array<Any>) {

    override fun toString() = testData.joinToString(
        prefix = "[{",
        postfix = "}]",
        separator = "}, {"
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestNgDataSet
        if (!testData.contentEquals(other.testData)) return false

        return true
    }

    override fun hashCode(): Int {
        return testData.contentHashCode()
    }

}