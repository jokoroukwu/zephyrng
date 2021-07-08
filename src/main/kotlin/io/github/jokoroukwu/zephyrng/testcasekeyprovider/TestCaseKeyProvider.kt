package io.github.jokoroukwu.zephyrng.testcasekeyprovider

import org.testng.ITestNGMethod

interface TestCaseKeyProvider {

    fun getTestCaseKey(method: ITestNGMethod): String?
}