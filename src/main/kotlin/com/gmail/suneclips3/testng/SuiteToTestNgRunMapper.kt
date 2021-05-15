package com.gmail.suneclips3.testng

import com.gmail.suneclips3.TestResult
import com.gmail.suneclips3.TestResults
import com.gmail.suneclips3.TestDataEntry
import com.gmail.suneclips3.TestNgTestRun
import com.gmail.suneclips3.annotations.TestCaseKey
import com.gmail.suneclips3.annotations.ZephyrStep
import org.testng.ISuite
import org.testng.ITestNGMethod
import org.testng.ITestResult
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SuiteToTestNgRunMapper(
    private val testDataInterceptor: ITestDataSetProvider = TestDataInterceptor(),
    private val clock: Clock = Clock.system(ZoneOffset.ofHours(3))
) {

    fun mapSuiteToTestNgTestRun(suite: ISuite): TestNgTestRun {
        val testRunResultMap = HashMap<String, TestResults>()
        for (testResult in suite.results.values) {
            val testContext = testResult.testContext
            processSkippedOrFailedTests(testContext.failedTests.allResults, testRunResultMap)
            processSkippedOrFailedTests(testContext.skippedTests.allResults, testRunResultMap)
            processPassedTests(testContext.passedTests.allResults, testRunResultMap)
        }
        val testRunName = "${resolveSuiteName(suite)}-${Instant.now(clock)}"
        return TestNgTestRun(testRunName, testRunResultMap)
    }

    private fun resolveSuiteName(suite: ISuite) =
        if (suite.name.isNullOrEmpty()) "Anonymous-suite" else suite.name

    private fun processSkippedOrFailedTests(
        testResults: Set<ITestResult>,
        testResultsMap: MutableMap<String, TestResults>
    ) {
        for (result in testResults) {
            val testCaseKey = findTestCaseKey(result.method)
            if (testCaseKey != null) {
                val failedStepIndex = findFailedStep(result)
                addResult(
                    testNgResult = result,
                    testResultsMap = testResultsMap,
                    testCaseKey = testCaseKey,
                    hasPassed = false,
                    failedStepIndex = failedStepIndex
                )
            }
        }
    }

    private fun processPassedTests(
        testNGTestResults: Set<ITestResult>,
        testResultsMap: MutableMap<String, TestResults>
    ) {
        for (result in testNGTestResults) {
            val testCaseKey = findTestCaseKey(result.method)
            if (testCaseKey != null) {
                addResult(
                    testNgResult = result,
                    testResultsMap = testResultsMap,
                    testCaseKey = testCaseKey,
                    hasPassed = true,
                )
            }
        }
    }

    private fun addResult(
        testNgResult: ITestResult,
        testResultsMap: MutableMap<String, TestResults>,
        testCaseKey: String,
        hasPassed: Boolean,
        failedStepIndex: Int? = null
    ) {
        val testCaseResults = testResultsMap.computeIfAbsent(testCaseKey) { TestResults() }
        if (!testNgResult.method.isDataDriven) {
            testCaseResults.addResult(
                TestResult(
                    index = 0,
                    failedStepIndex = failedStepIndex,
                    hasPassed = hasPassed
                )
            )
        } else {
            val testData = TestDataEntry(testNgResult.parameters)
            val testResultIndex =
                testDataInterceptor.getDataSetIndex(testNgResult.method, testData)
            if (testResultIndex != null) {
                testCaseResults.addResult(
                    TestResult(
                        index = testResultIndex,
                        failedStepIndex = failedStepIndex,
                        hasPassed = hasPassed
                    )
                )
            }
            //  should never happen
            else {
                println(
                    "WARNING: test result will not be submitted" +
                            " {reason: no index for provided data set," +
                            " testCaseKey: $testCaseKey, testMethod: ${testNgResult.method}, dataSet: $testData}"
                )
            }
        }
    }

    private fun findTestCaseKey(method: ITestNGMethod): String? {
        val methodTestCaseKey = method.constructorOrMethod.method.getDeclaredAnnotation(TestCaseKey::class.java)?.value
        return if (methodTestCaseKey != null) {
            methodTestCaseKey
        } else {
            val declaredAnnotation: Annotation? =
                method.realClass.getDeclaredAnnotation<Annotation>(TestCaseKey::class.java)
            if (declaredAnnotation is TestCaseKey) declaredAnnotation.value else null
        }
    }

    private fun isWithinTestClassScope(testClass: Class<*>, currentClass: Class<*>) =
        testClass == currentClass || currentClass.isAssignableFrom(testClass)

    private fun findFailedStep(testResult: ITestResult): Int? {
        val testClass = testResult.testClass.realClass
        val stackTraceElements = testResult.throwable.stackTrace
        var stepIndex: Int? = null
        var i = 0
        val k = stackTraceElements.size
        while (i < k) {
            var stackTraceElement = stackTraceElements[i]
            var nextClass = Class.forName(stackTraceElement.className)
            var zephyrStep = walkClassMethods(nextClass, stackTraceElement.methodName)
            if (zephyrStep != null) {
                stepIndex = zephyrStep.value
            }
            if (isWithinTestClassScope(testClass, nextClass)) {
                //  now search within scope of test class and its ancestors
                do {
                    if (++i == k) {
                        return stepIndex
                    }
                    stackTraceElement = stackTraceElements[i]
                    nextClass = Class.forName(stackTraceElement.className)
                    zephyrStep = walkClassMethods(nextClass, stackTraceElement.methodName)
                    if (zephyrStep != null) {
                        stepIndex = zephyrStep.value
                    }
                } while (isWithinTestClassScope(testClass, nextClass))
            }
            i++
        }
        return stepIndex
    }

    private fun walkClassMethods(elementClass: Class<*>, expectedMethodName: String): ZephyrStep? {
        return elementClass.declaredMethods.find { method -> method.name == expectedMethodName }
            ?.getDeclaredAnnotation(ZephyrStep::class.java)
            ?.apply {
                println(
                    "found ZephyrStep: {description: $description, index: $value, " +
                            "class: ${elementClass.simpleName}, method: $expectedMethodName}"
                )
            }
    }
}