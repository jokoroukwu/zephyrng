package com.gmail.suneclips3.tests.testkeyskeyfinder


import com.gmail.suneclips3.annotations.TestCaseKey
import com.gmail.suneclips3.testcasekeyfinder.TestCaseKeyFinder
import com.gmail.suneclips3.tests.Arguments
import io.mockk.every
import io.mockk.mockk
import org.testng.ITestNGMethod
import org.testng.annotations.DataProvider
import org.testng.internal.ConstructorOrMethod
import kotlin.test.Test
import kotlin.test.assertEquals

const val classKey = "class-key"
const val methodKey = "method-key"

class TestCaseKeyFinderTest {

    @Test(dataProvider = "methodProvider")
    fun `should return expected test case key`(arguments: Arguments) {
        val testNgMethod: ITestNGMethod = mockk()

        every { testNgMethod.realClass } returns arguments.testClass
        every { testNgMethod.constructorOrMethod } returns ConstructorOrMethod(arguments.testMethod)

        assertEquals(arguments.expectedKey, TestCaseKeyFinder.findTestCaseKey(testNgMethod))
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