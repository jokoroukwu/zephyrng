package com.github.jokoroukwu.zephyrng.tests.testresulttodatasetmapper

import com.github.jokoroukwu.zephyrng.http.detailedreport.TestCase
import com.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrStepResult
import com.github.jokoroukwu.zephyrng.http.detailedreport.ZephyrTestResult
import com.github.jokoroukwu.zephyrng.updatableresult.ZephyrDataSets
import com.github.jokoroukwu.zephyrng.updatableresult.ZephyrResultToDataSetMapperImpl
import org.assertj.core.api.Assertions
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestResultToDataSetMapperTest {
    private val zephyrResultToDataSetMapper = ZephyrResultToDataSetMapperImpl

    @Test(dataProvider = "testResultProvider")
    fun `should return expected datasets`(zephyrTestResult: ZephyrTestResult, expectedDataSets: ZephyrDataSets) {
        val datasets = zephyrResultToDataSetMapper.mapTestResultToZephyrDataSets(zephyrTestResult)
        Assertions.assertThat(datasets)
            .`as`("data sets validation")
            .isEqualTo(expectedDataSets)
    }

    @DataProvider
    private fun testResultProvider(): Array<Array<Any>> {
        val dummyTestCase = TestCase(0, "test-key", "test-name")
        val dataSetOne = listOf(ZephyrStepResult(0, 0), ZephyrStepResult(1, 1))
        val dataSetTwo = listOf(ZephyrStepResult(2, 0), ZephyrStepResult(3, 1))
        return arrayOf(
            arrayOf(
                ZephyrTestResult(0, dummyTestCase, dataSetOne + dataSetTwo),
                listOf(dataSetOne, dataSetTwo)
            ),
            arrayOf(ZephyrTestResult(0, dummyTestCase, dataSetOne), listOf(dataSetOne)),
            arrayOf(ZephyrTestResult(0, dummyTestCase, emptyList()), emptyList<ZephyrStepResult>())
        )
    }
}