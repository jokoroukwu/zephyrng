package io.github.jokoroukwu.zephyrng.testcasekeyprovider

import io.github.jokoroukwu.zephyrapi.annotations.TestCaseKey
import org.testng.ITestNGMethod

object TestCaseKeyProviderImpl : TestCaseKeyProvider {

    override fun getTestCaseKey(method: ITestNGMethod): String? {
        val methodTestCaseKey = method.constructorOrMethod.method.getDeclaredAnnotation(TestCaseKey::class.java)?.value
        return if (methodTestCaseKey != null) {
            methodTestCaseKey
        } else {
            val declaredAnnotation: Annotation? =
                method.realClass.getDeclaredAnnotation<Annotation>(TestCaseKey::class.java)
            if (declaredAnnotation is TestCaseKey) declaredAnnotation.value.trim() else null
        }
    }
}