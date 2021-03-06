package io.github.jokoroukwu.zephyrng.datasetindexprovider

import org.testng.ITestNGMethod

/**
 * A container for TestNg method combined with a single data set,
 * returned by [org.testng.annotations.DataProvider] iterator
 * @see [TestDataIndexProvider]
 */
class TestDataID(private val testNGMethod: ITestNGMethod, private val dataSet: Array<Any?>) {


    override fun toString(): String {
        return "{testNG_method: '$testNGMethod', dataset: ${dataSet.dataSetToString()}}"
    }

    private fun Array<Any?>.dataSetToString() = joinToString(
        prefix = "['",
        postfix = "']",
        separator = "', '"
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestDataID

        if (testNGMethod != other.testNGMethod) return false
        if (!dataSet.contentEquals(other.dataSet)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = testNGMethod.hashCode()
        result = 31 * result + dataSet.contentHashCode()
        return result
    }
}