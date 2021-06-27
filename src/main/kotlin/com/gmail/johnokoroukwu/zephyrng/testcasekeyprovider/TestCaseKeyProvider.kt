package com.gmail.johnokoroukwu.zephyrng.testcasekeyprovider

import org.testng.ITestNGMethod

interface TestCaseKeyProvider {

    fun getTestCaseKey(method: ITestNGMethod): String?
}