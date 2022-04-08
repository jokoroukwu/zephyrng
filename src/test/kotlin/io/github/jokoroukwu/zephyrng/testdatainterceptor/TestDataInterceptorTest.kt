package io.github.jokoroukwu.zephyrng.testdatainterceptor

import io.github.jokoroukwu.zephyrng.datasetindexprovider.TestDataID
import io.github.jokoroukwu.zephyrng.datasetindexprovider.TestDataIndexProviderBase
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.testng.ITestNGMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class TestDataInterceptorTest {

    @Test(dataProvider = "mapTestProvider")
    fun `should add expected values to map`(
        iterator: MutableIterator<Array<Any?>>,
        testNgMethod: ITestNGMethod?,
        expectedMapContent: Map<TestDataID, Queue<Int>>
    ) {
        val map: MutableMap<TestDataID, Queue<Int>> = HashMap()
        val testDataInterceptor = TestDataIndexProviderBase(map)
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
                    TestDataID(testNgMethodMock, dataSetOne) to ConcurrentLinkedQueue(listOf(0)),
                    TestDataID(testNgMethodMock, dataSetTwo) to ConcurrentLinkedQueue(listOf(1)),
                )
            ),
            arrayOf(
                mutableListOf(dataSetOne, dataSetTwo, dataSetOne).iterator(),
                testNgMethodMock,
                hashMapOf(
                    TestDataID(testNgMethodMock, dataSetOne) to ConcurrentLinkedQueue(listOf(0, 2)),
                    TestDataID(testNgMethodMock, dataSetTwo) to ConcurrentLinkedQueue(listOf(1)),
                )
            ),
            arrayOf(
                differentArgIterator,
                null,
                emptyMap<TestDataID, MutableList<Int>>()
            ),
            arrayOf(
                Collections.emptyIterator<Array<Any>>(),
                testNgMethodMock,
                emptyMap<TestDataID, MutableList<Int>>()
            )
        )
    }

    @Test(dataProvider = "returnValueTestProvider")
    fun `should return expected value`(
        iterator: MutableIterator<Array<Any?>>,
        testNgMethod: ITestNGMethod?,
        expectedContentList: List<Array<Any>>?
    ) {
        val testDataInterceptor = TestDataIndexProviderBase()
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