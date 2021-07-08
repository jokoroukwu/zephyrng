package io.github.jokoroukwu.zephyrng.tests.testresulttoidmapper

import io.github.jokoroukwu.zephyrng.http.ZephyrException
import io.github.jokoroukwu.zephyrng.http.testresultstatus.GetTestResultStatusesRequestSender
import io.github.jokoroukwu.zephyrng.http.testresultstatus.SerializableTestResultStatusItem
import io.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import io.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatusToIdMapProvider
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestResultToIdMapperTestStatus {
    private lateinit var getTestResultStatusesRequestSender: GetTestResultStatusesRequestSender
    private val passItem = SerializableTestResultStatusItem(0, TestResultStatus.PASS)
    private val failItem = SerializableTestResultStatusItem(1, TestResultStatus.FAIL)
    private val blockedItem = SerializableTestResultStatusItem(2, TestResultStatus.BLOCKED)
    private val notExecutedItem = SerializableTestResultStatusItem(3, TestResultStatus.NOT_EXECUTED)
    private val inProgressItem = SerializableTestResultStatusItem(4, TestResultStatus.IN_PROGRESS)


    @BeforeMethod
    fun setUp() {
        getTestResultStatusesRequestSender = mockk()
    }

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearMocks(getTestResultStatusesRequestSender)
    }

    @Test(dataProvider = "resultItemsProvider")
    fun `should throw exception when no id mapping for test result status`(statusItems: List<SerializableTestResultStatusItem>) {
        every { getTestResultStatusesRequestSender.getTestResultStatusesRequest(any()) } returns statusItems

        Assertions.assertThatThrownBy {
            TestResultStatusToIdMapProvider(getTestResultStatusesRequestSender).getTestResultStatusToIdMap(1)
        }.isInstanceOf(ZephyrException::class.java)
    }

    @DataProvider
    fun resultItemsProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(listOf(failItem, notExecutedItem, blockedItem, inProgressItem)),
            arrayOf(listOf(passItem, notExecutedItem, blockedItem, inProgressItem)),
            arrayOf(listOf(passItem, failItem, blockedItem, inProgressItem)),
            arrayOf(listOf(passItem, failItem, notExecutedItem, inProgressItem)),
            arrayOf(listOf(passItem, failItem, notExecutedItem, blockedItem))
        )
    }


    @Test
    fun `should return expected test result to id map`() {
        val testResultStatusItems = listOf(
            passItem, failItem, inProgressItem,
            notExecutedItem, blockedItem
        )
        val expectedResultMap = hashMapOf<TestResultStatus, Long>(
            TestResultStatus.PASS to 0,
            TestResultStatus.FAIL to 1,
            TestResultStatus.BLOCKED to 2,
            TestResultStatus.NOT_EXECUTED to 3,
            TestResultStatus.IN_PROGRESS to 4
        )
        every { getTestResultStatusesRequestSender.getTestResultStatusesRequest(any()) } returns testResultStatusItems

        val actualResultMap =
            TestResultStatusToIdMapProvider(getTestResultStatusesRequestSender).getTestResultStatusToIdMap(1)

        Assertions.assertThat(actualResultMap).containsAllEntriesOf(expectedResultMap)
    }
}