package com.gmail.suneclips3.testcasekeyfinder

import com.gmail.suneclips3.annotations.TestCaseKey
import org.testng.ITestNGMethod

object TestCaseKeyFinder : ITestCaseKeyFinder {

    override fun findTestCaseKey(method: ITestNGMethod): String? {
        val methodTestCaseKey = method.constructorOrMethod.method.getDeclaredAnnotation(TestCaseKey::class.java)?.value
        return if (methodTestCaseKey != null) {
            methodTestCaseKey
        } else {
            val declaredAnnotation: Annotation? =
                method.realClass.getDeclaredAnnotation<Annotation>(TestCaseKey::class.java)
            if (declaredAnnotation is TestCaseKey) declaredAnnotation.value else null
        }
    }
}