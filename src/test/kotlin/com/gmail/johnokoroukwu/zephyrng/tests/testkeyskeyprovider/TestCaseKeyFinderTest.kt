package com.gmail.johnokoroukwu.zephyrng.tests.testkeyskeyprovider


import com.gmail.johnokoroukwu.zephyrng.annotations.TestCaseKey
import com.gmail.johnokoroukwu.zephyrng.testcasekeyprovider.TestCaseKeyProviderImpl
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.testng.ITestNGMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.testng.internal.ConstructorOrMethod
import java.lang.reflect.Method

const val classKey = "class-key"
const val methodKey = "method-key"

class TestCaseKeyFinderTest {

    @Test(dataProvider = "methodProvider")
    fun `should return expected test case key`(arguments: Arguments) {
        val testNgMethod: ITestNGMethod = mockk()

        every { testNgMethod.realClass } returns arguments.testClass
        every { testNgMethod.constructorOrMethod } returns ConstructorOrMethod(arguments.testMethod)

        Assertions.assertThat(arguments.expectedKey)
            .`as`("expected key")
            .isEqualTo(TestCaseKeyProviderImpl.getTestCaseKey(testNgMethod))
    }

    @DataProvider
    private fun methodProvider(): Array<Arguments> {
        val unAnnotatedClass = ClassWithNoTestCaseKeyAnnotation::class.java
        val unAnnotatedClassMethod = unAnnotatedClass.getDeclaredMethod("unAnnotatedMethod")

        val annotatedClass = ClassWithTestCaseKeyAnnotation::class.java
        val annotatedClassUnAnnotatedMethod = annotatedClass.getDeclaredMethod("unAnnotatedMethod")
        val annotatedClassAnnotatedMethod = annotatedClass.getDeclaredMethod("annotatedMethod")

        return arrayOf(
            Arguments(annotatedClass, annotatedClassAnnotatedMethod, methodKey),
            Arguments(annotatedClass, annotatedClassUnAnnotatedMethod, classKey),
            Arguments(unAnnotatedClass, unAnnotatedClassMethod, null)
        )
    }


    @TestCaseKey(classKey)
    private class ClassWithTestCaseKeyAnnotation {

        private fun unAnnotatedMethod() {

        }

        @TestCaseKey(methodKey)
        private fun annotatedMethod() {

        }
    }

    private class ClassWithNoTestCaseKeyAnnotation {

        private fun unAnnotatedMethod() {

        }
    }
}

//   This wrapper is require because otherwise TestNG will try to inject method object into test method
class Arguments(val testClass: Class<*>, val testMethod: Method, val expectedKey: String?)