package com.gmail.suneclips3.testng

import com.gmail.suneclips3.TestDataEntry
import org.testng.ITestNGMethod

interface ITestDataSetProvider {
    fun getDataSetIndex(testNGMethod: ITestNGMethod, testDataEntry: TestDataEntry): Int?
}
