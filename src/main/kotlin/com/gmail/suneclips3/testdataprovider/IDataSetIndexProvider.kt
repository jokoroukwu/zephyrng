package com.gmail.suneclips3.testdataprovider

import com.gmail.suneclips3.TestNgDataSet
import org.testng.ITestNGMethod

interface IDataSetIndexProvider {
    fun getDataSetIndexAndRemove(testNGMethodToNgDataSet: Pair<ITestNGMethod, TestNgDataSet>): Int?
}
