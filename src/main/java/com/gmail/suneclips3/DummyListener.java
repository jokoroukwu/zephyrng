package com.gmail.suneclips3;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import java.util.List;

public class DummyListener implements IReporter {

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        for (ISuite suite : suites) {
            for (ISuiteResult value : suite.getResults().values()) {
                for (ITestResult allResult : value.getTestContext().getSkippedTests().getAllResults()) {
                    var ok = 1;
                }
            }
        }
    }
}
