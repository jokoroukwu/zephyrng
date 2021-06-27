package com.gmail.johnokoroukwu.zephyrng.tests.failedstepprovider

import com.gmail.johnokoroukwu.zephyrng.failedstepprovider.FailedStepProviderImpl
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.testng.ITestResult
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class FailedStepFinderTest {
    private val testResultMock: ITestResult = mockk()
    private val testClass = TestClass::class.java


    @Test(dataProvider = "dataProvider")
    fun `should return expected step index`(throwable: Throwable?, expectedStepIndex: Int?) {
        every { testResultMock.testClass.realClass } returns testClass
        every { testResultMock.throwable } returns throwable

        Assertions.assertThat(expectedStepIndex)
            .`as`("expected step index")
            .isEqualTo(FailedStepProviderImpl.getFailedStep(testResultMock)?.value)
    }

    @DataProvider
    private fun dataProvider(): Array<Array<Any?>> {
        val instance = TestClass()
        val plainMethod = instance.runCatching(TestClass::methodWithStep).exceptionOrNull()!!
        val nestedMethodThrowable =
            instance.runCatching(TestClass::callNestedStepMethod).exceptionOrNull()!!
        val overriddenMethodThrowable =
            instance.runCatching(TestClass::overriddenWithStep).exceptionOrNull()!!
        val otherClassMethodThrowable =
            instance.runCatching(TestClass::callOtherClass).exceptionOrNull()!!

        return arrayOf(
            arrayOf(plainMethod, STEP_INDEX),
            arrayOf(nestedMethodThrowable, NESTED_STEP_INDEX),
            arrayOf(overriddenMethodThrowable, OVERRIDDEN_STEP_INDEX),
            arrayOf(otherClassMethodThrowable, OTHER_CLASS_STEP_INDEX),
            arrayOf(null, null)
        )
    }
}