package com.gmail.suneclips3.tests

import org.testng.IReporter
import org.testng.ISuite
import org.testng.xml.XmlSuite

class DummyTestListener : IReporter {
    override fun generateReport(
        xmlSuites: MutableList<XmlSuite>?,
        suites: MutableList<ISuite>?,
        outputDirectory: String?
    ) {
        val ok = 1
    }
}