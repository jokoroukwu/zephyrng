package com.gmail.suneclips3.testng

import com.gmail.suneclips3.TestDataEntry
import org.testng.IDataProviderInterceptor
import org.testng.IDataProviderMethod
import org.testng.ITestContext
import org.testng.ITestNGMethod
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TestDataInterceptor(
    private val testMethodToTestDataToIndexMap: MutableMap<ITestNGMethod, MutableMap<TestDataEntry, Int>> = ConcurrentHashMap()
) : IDataProviderInterceptor, ITestDataSetProvider {

    override fun intercept(
        original: MutableIterator<Array<Any>>?,
        dataProviderMethod: IDataProviderMethod?,
        method: ITestNGMethod?,
        iTestContext: ITestContext?
    ): MutableIterator<Array<Any>> {
        return if (original != null) {
            val copy = ArrayList<Array<Any>>()
            val testDataToIndexMap = ConcurrentHashMap<TestDataEntry, Int>(5)
            var index = 0
            while (original.hasNext()) {
                val data = original.next()
                testDataToIndexMap[TestDataEntry(data)] = index++
                copy.add(data)
            }
            //  method may never be null
            testMethodToTestDataToIndexMap[method!!] = testDataToIndexMap
            copy.iterator()
        } else {
            Collections.emptyListIterator()
        }
    }

    override fun getDataSetIndex(testNGMethod: ITestNGMethod, testDataEntry: TestDataEntry): Int? {
        return testMethodToTestDataToIndexMap[testNGMethod]?.remove(testDataEntry)
    }
}