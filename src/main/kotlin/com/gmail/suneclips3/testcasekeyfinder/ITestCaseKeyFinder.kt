package com.gmail.suneclips3.testcasekeyfinder

import org.testng.ITestNGMethod

interface ITestCaseKeyFinder {

    fun findTestCaseKey(method: ITestNGMethod): String?
}