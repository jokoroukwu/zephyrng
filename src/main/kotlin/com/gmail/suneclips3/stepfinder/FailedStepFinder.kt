package com.gmail.suneclips3.stepfinder

import com.gmail.suneclips3.annotations.ZephyrStep
import org.testng.ITestResult

object FailedStepFinder : IFailedStepFinder {

    override fun findFailedStep(testResult: ITestResult): ZephyrStep? {
        val testClass = testResult.testClass.realClass
        return testResult.throwable?.stackTrace?.takeIf(Array<StackTraceElement>::isNotEmpty)?.let {
            var failedStep: ZephyrStep? = null
            var i = 0
            var currentElement = it[i]
            var currentClass = Class.forName(currentElement.className)

            while (!isWithinTestClassScope(testClass, currentClass)) {
                val step = walkClassMethods(currentClass, currentElement.methodName)
                if (step != null) {
                    failedStep = step
                }
                currentElement = it[++i]
                currentClass = Class.forName(currentElement.className)
            }
            do {
                val step = walkClassMethods(currentClass, currentElement.methodName)
                if (step != null) {
                    failedStep = step
                }
                currentElement = it[++i]
                currentClass = Class.forName(currentElement.className)

            } while (isWithinTestClassScope(testClass, currentClass))

            return failedStep
        }
    }

    private fun isWithinTestClassScope(testClass: Class<*>, currentClass: Class<*>) =
        testClass == currentClass || currentClass.isAssignableFrom(testClass)

    private fun walkClassMethods(elementClass: Class<*>, expectedMethodName: String): ZephyrStep? {
        return elementClass.declaredMethods.find { it.name == expectedMethodName }
            ?.getDeclaredAnnotation(ZephyrStep::class.java)
            ?.apply {
                println(
                    "TRACE found ZephyrStep annotation: {description: ${description.ifEmpty { "<none>" }}, index: $value, " +
                            "class: ${elementClass.simpleName}, method: $expectedMethodName}"
                )
            }
    }
}