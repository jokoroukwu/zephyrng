package com.gmail.suneclips3.suiteexecutionstate

import org.testng.ISuite
import org.testng.ISuiteListener
import java.time.Instant

object SuiteExecutionStateListener : ISuiteListener {

    override fun onStart(suite: ISuite?) {
        suite?.setAttribute(SuiteAttribute.START_TIME, Instant.now()) ?: logWarning()
    }

    override fun onFinish(suite: ISuite?) {
        suite?.setAttribute(SuiteAttribute.END_TIME, Instant.now()) ?: logWarning()
    }

    private fun logWarning() = println("WARING null suite")
}