package io.github.jokoroukwu.zephyrng.datasetindexprovider

import org.testng.IDataProviderInterceptor
import org.testng.IDataProviderMethod
import org.testng.ITestContext
import org.testng.ITestNGMethod
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Adds support to publish data driven test results to Zephyr.
 *
 * Intercepts iterator of [org.testng.annotations.DataProvider] method,
 * mapping each dataset to its index in ascending order returned by the iterator
 */
class TestDataIndexProviderBase(
    private val testDataIdToTestDataIndexMap: MutableMap<TestDataID, Queue<Int>> = ConcurrentHashMap()
) : IDataProviderInterceptor, TestDataIndexProvider {

    override fun intercept(
        original: MutableIterator<Array<Any?>>?,
        dataProviderMethod: IDataProviderMethod?,
        method: ITestNGMethod?,
        iTestContext: ITestContext?
    ): Iterator<Array<Any?>>? {

        return when {
            // should never happen
            (method == null || original == null) -> original

            original.hasNext() -> {
                val copy = LinkedList<Array<Any?>>()
                original.withIndex().forEach {
                    testDataIdToTestDataIndexMap.computeIfAbsent(TestDataID(method, it.value))
                    { ConcurrentLinkedQueue() }.offer(it.index)
                    copy.add(it.value)
                }
                copy.iterator()
            }
            else -> original
        }
    }

    override fun pollDataSetIndex(testDataID: TestDataID) = testDataIdToTestDataIndexMap[testDataID]?.poll()
}