package com.gmail.suneclips3.testdataprovider

import com.gmail.suneclips3.TestNgDataSet
import org.testng.IDataProviderInterceptor
import org.testng.IDataProviderMethod
import org.testng.ITestContext
import org.testng.ITestNGMethod
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class TestDataInterceptor(
    private val testMethodToTestNgDataToIndexMap: MutableMap<Pair<ITestNGMethod, TestNgDataSet>, Queue<Int>> =
        ConcurrentHashMap()
) : IDataProviderInterceptor, IDataSetIndexProvider {

    override fun intercept(
        original: MutableIterator<Array<Any>>?,
        dataProviderMethod: IDataProviderMethod?,
        method: ITestNGMethod?,
        iTestContext: ITestContext?
    ): MutableIterator<Array<Any>> {

        return when {
            method == null -> {
                println("WARN null testNGMethod")
                original!!
            }
            original!!.hasNext() -> {
                val copy = LinkedList<Array<Any>>()
                original.withIndex().forEach {
                    testMethodToTestNgDataToIndexMap.computeIfAbsent(Pair(method, TestNgDataSet(it.value)))
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

    override fun getDataSetIndexAndRemove(testNGMethodToNgDataSet: Pair<ITestNGMethod, TestNgDataSet>): Int? {
        return testMethodToTestNgDataToIndexMap[testNGMethodToNgDataSet]?.poll()
    }
}