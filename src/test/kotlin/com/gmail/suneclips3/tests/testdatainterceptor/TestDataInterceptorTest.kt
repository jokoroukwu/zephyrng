package com.gmail.suneclips3.tests.testdatainterceptor

import com.gmail.suneclips3.TestNgDataSet
import com.gmail.suneclips3.testdataprovider.TestDataInterceptor
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
        iterator: MutableIterator<Array<Any>>,
        testNgMethod: ITestNGMethod?,
        expectedMapContent: Map<Pair<ITestNGMethod, TestNgDataSet>, Queue<Int>>
    ) {
        val map: MutableMap<Pair<ITestNGMethod, TestNgDataSet>, Queue<Int>> = HashMap()
        val testDataInterceptor = TestDataInterceptor(map)
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
        val dataSetOne = arrayOf<Any>("data set one")
        val dataSetTwo = arrayOf<Any>("data set two")

        val differentArgIterator = mutableListOf(dataSetOne, dataSetTwo).iterator()
        return arrayOf(
            arrayOf(
                differentArgIterator,
                testNgMethodMock,
                hashMapOf(
                    Pair(testNgMethodMock, TestNgDataSet(dataSetOne)) to ConcurrentLinkedQueue(listOf(0)),
                    Pair(testNgMethodMock, TestNgDataSet(dataSetTwo)) to ConcurrentLinkedQueue(listOf(1)),
                )
            ),
            arrayOf(
                mutableListOf(dataSetOne, dataSetTwo, dataSetOne).iterator(),
                testNgMethodMock,
                hashMapOf(
                    Pair(testNgMethodMock, TestNgDataSet(dataSetOne)) to ConcurrentLinkedQueue(listOf(0, 2)),
                    Pair(testNgMethodMock, TestNgDataSet(dataSetTwo)) to ConcurrentLinkedQueue(listOf(1)),
                )
            ),
            arrayOf(
                differentArgIterator,
                null,
                emptyMap<Pair<ITestNGMethod, TestNgDataSet>, MutableList<Int>>()
            ),
            arrayOf(
                Collections.emptyIterator<Array<Any>>(),
                testNgMethodMock,
                emptyMap<Pair<ITestNGMethod, TestNgDataSet>, MutableList<Int>>()
            )
        )
    }

    @Test(dataProvider = "returnValueTestProvider")
    fun `should return expected value`(
        iterator: MutableIterator<Array<Any>>,
        testNgMethod: ITestNGMethod?,
        expectedContentList: List<Array<Any>>?
    ) {
        val testDataInterceptor = TestDataInterceptor()
        val actualIterator = testDataInterceptor.intercept(iterator, null, testNgMethod, null)

        val actualContentList = LinkedList<Array<Any>>()
        actualIterator.forEachRemaining(actualContentList::add)

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