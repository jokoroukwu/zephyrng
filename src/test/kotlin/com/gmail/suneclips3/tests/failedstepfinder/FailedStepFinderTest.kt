package com.gmail.suneclips3.tests.failedstepfinder

import com.gmail.suneclips3.stepfinder.FailedStepFinder
import io.mockk.every
import io.mockk.mockk
import org.testng.ITestResult
import org.testng.annotations.DataProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FailedStepFinderTest {
    private val testResultMock: ITestResult = mockk()
    private val testClass = TestClass::class.java


    @Test(dataProvider = "dataProvider")
    fun `should return expected step index`(throwable: Throwable?, expectedStepIndex: Int?) {
        every { testResultMock.testClass.realClass } returns testClass
        every { testResultMock.throwable } returns throwable

        assertEquals(expectedStepIndex, FailedStepFinder.findFailedStep(testResultMock)?.value)
    }

    @DataProvider
    private fun dataProvider(): Array<Array<Any?>> {
        val failureMessage = "expected method call to raise exception"
        val instance = TestClass()

        val plainMethod = instance.runCatching(TestClass::methodWithStep).exceptionOrNull() ?: fail(failureMessage)
        val nestedMethodThrowable =
            instance.runCatching(TestClass::callNestedStepMethod).exceptionOrNull() ?: fail(failureMessage)
        val overriddenMethodThrowable =
            instance.runCatching(TestClass::overriddenWithStep).exceptionOrNull() ?: fail(failureMessage)
        val otherClassMethodThrowable =
            instance.runCatching(TestClass::callOtherClass).exceptionOrNull() ?: fail(failureMessage)

        return arrayOf(
            arrayOf(plainMethod, STEP_INDEX),
            arrayOf(nestedMethodThrowable, NESTED_STEP_INDEX),
            arrayOf(overriddenMethodThrowable, OVERRIDDEN_STEP_INDEX),
            arrayOf(otherClassMethodThrowable, OTHER_CLASS_STEP_INDEX),
            arrayOf(null, null)
        )
    }
}