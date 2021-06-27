package com.gmail.johnokoroukwu.zephyrng.tests.testdatainterceptor

import com.gmail.johnokoroukwu.zephyrng.datasetindexprovider.DataSetID
import com.gmail.johnokoroukwu.zephyrng.datasetindexprovider.DataSetIndexProviderImpl
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.testng.ITestNGMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class TestDataInterceptorTest {

    @Test
    fun `should remove expected values from map`() {

    }

    @Test(dataProvider = "mapTestProvider")
    fun `should add expected values to map`(
        iterator: MutableIterator<Array<Any?>>,
        testNgMethod: ITestNGMethod?,
        expectedMapContent: Map<DataSetID, Queue<Int>>
    ) {
        val map: MutableMap<DataSetID, Queue<Int>> = HashMap()
        val testDataInterceptor = DataSetIndexProviderImpl(map)
        testDataInterceptor.intercept(iterator, null, testNgMethod, null)

        val softly = SoftAssertions()
        for (entry in expectedMapContent.entries) {
            val actualValue = map[entry.key]
            softly.assertThat(actualValue)
                .`as`("key: ${entry.key}")
                .containsExactlyInAnyOrderElementsOf(entry.value)
        }
        softly.assertAll()
    }

    @DataProvider
    private fun mapTestProvider(): Array<Array<Any?>> {
        val testNgMethodMock = mockk<ITestNGMethod>()
        val dataSetOne = arrayOf<Any?>("data set one")
        val dataSetTwo = arrayOf<Any?>("data set two")

        val differentArgIterator = mutableListOf(dataSetOne, dataSetTwo).iterator()
        return arrayOf(
            arrayOf(
                differentArgIterator,
                testNgMethodMock,
                hashMapOf(
                    DataSetID(testNgMethodMock, dataSetOne) to ConcurrentLinkedQueue(listOf(0)),
                    DataSetID(testNgMethodMock, dataSetTwo) to ConcurrentLinkedQueue(listOf(1)),
                )
            ),
            arrayOf(
                mutableListOf(dataSetOne, dataSetTwo, dataSetOne).iterator(),
                testNgMethodMock,
                hashMapOf(
                    DataSetID(testNgMethodMock, dataSetOne) to ConcurrentLinkedQueue(listOf(0, 2)),
                    DataSetID(testNgMethodMock, dataSetTwo) to ConcurrentLinkedQueue(listOf(1)),
                )
            ),
            arrayOf(
                differentArgIterator,
                null,
                emptyMap<DataSetID, MutableList<Int>>()
            ),
            arrayOf(
                Collections.emptyIterator<Array<Any>>(),
                testNgMethodMock,
                emptyMap<DataSetID, MutableList<Int>>()
            )
        )
    }

    @Test(dataProvider = "returnValueTestProvider")
    fun `should return expected value`(
        iterator: MutableIterator<Array<Any?>>,
        testNgMethod: ITestNGMethod?,
        expectedContentList: List<Array<Any>>?
    ) {
        val testDataInterceptor = DataSetIndexProviderImpl()
        val actualIterator = testDataInterceptor.intercept(iterator, null, testNgMethod, null)

        val actualContentList = LinkedList<Array<Any?>>()
        actualIterator!!.forEachRemaining(actualContentList::add)

        Assertions.assertThat(expectedContentList).isEqualTo(actualContentList)
    }

    @DataProvider
    private fun returnValueTestProvider(): Array<Array<Any?>> {
        val testNgMethodMock = mockk<ITestNGMethod>()
        val dataSetOne = arrayOf<Any>("data set one")
        val dataSetTwo = arrayOf<Any>("data set two")

        val contentList = mutableListOf(dataSetOne, dataSetTwo)
        return arrayOf(
            arrayOf(contentList.iterator(), testNgMethodMock, contentList),
            arrayOf(contentList.iterator(), null, contentList),
            arrayOf(Collections.emptyIterator<Array<Any>>(), testNgMethodMock, mutableListOf<Array<Any>>()),
        )
    }
}