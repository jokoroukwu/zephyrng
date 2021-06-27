package com.gmail.johnokoroukwu.zephyrng.datasetindexprovider

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
class DataSetIndexProviderImpl(
    private val testMethodToTestNgDataToIndexMap: MutableMap<DataSetID, Queue<Int>> = ConcurrentHashMap()
) : IDataProviderInterceptor, DataSetIndexProvider {

    override fun intercept(
        original: MutableIterator<Array<Any?>>?,
        dataProviderMethod: IDataProviderMethod?,
        method: ITestNGMethod?,
        iTestContext: ITestContext?
    ): MutableIterator<Array<Any?>>? {

        return when {
            (method == null || original == null) -> {
                // should never happen
                original
            }
            original.hasNext() -> {
                val copy = LinkedList<Array<Any?>>()
                original.withIndex().forEach {
                    testMethodToTestNgDataToIndexMap.computeIfAbsent(DataSetID(method, it.value))
                    { ConcurrentLinkedQueue() }.offer(it.index)
                    copy.add(it.value)
                }
                copy.iterator()

            }
            else -> {
                original
            }
        }
    }

    override fun pollDataSetIndex(dataSetID: DataSetID) =
        testMethodToTestNgDataToIndexMap[dataSetID]?.poll()
}