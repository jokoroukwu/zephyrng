package com.gmail.johnokoroukwu.zephyrng.tests.endtoend.util

import com.gmail.johnokoroukwu.zephyrng.annotations.TestCaseKey
import com.gmail.johnokoroukwu.zephyrng.annotations.ZephyrStep
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

const val DATA_DRIVEN_TEST_CASE_KEY = "data-driven-test-case-key"
const val NON_DATA_DRIVEN_TEST_CASE = "non-data-driven-test-case-key"
const val FAILED_STEP_INDEX = 1

class DummyTest {
    @BeforeClass
    fun preconditions() {

    }

    @TestCaseKey(DATA_DRIVEN_TEST_CASE_KEY)
    @Test(enabled = false)
    fun `data driven method`() {

    }

    @TestCaseKey(NON_DATA_DRIVEN_TEST_CASE)
    @Test(enabled = false)
    fun `non data driven method`() {

    }

    @ZephyrStep(FAILED_STEP_INDEX)
    fun someStep() {
    }

}