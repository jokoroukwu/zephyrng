package com.gmail.suneclips3.tests

import com.gmail.suneclips3.connectionconfig.ZephyrConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class Sandbox {


    //    @Test
    @Test(dataProvider = "testProvider")
    fun testName(value: Int) {
        val ok = 1
    }

    @Test
    fun testTwo() {
        println(Json.decodeFromString<ZephyrConfig>("""{"time-zone":"zone","jira-url":"url","project-key":"projectKey","username":"username","password":"pass"}"""))
    }

    @Test
    fun testThree() {

    }

    @DataProvider
    private fun testProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(1),
            arrayOf(2),
            arrayOf(3)
        )
    }
}

