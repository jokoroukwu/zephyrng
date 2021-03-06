package io.github.jokoroukwu.zephyrng.failedstepprovider

import io.github.jokoroukwu.zephyrapi.annotations.Step
import mu.KotlinLogging
import org.testng.ITestResult

val logger = KotlinLogging.logger { }

object FailedStepProviderImpl : FailedStepProvider {

    /**
     * Walks the stacktrace of provided TestNG test result in attempt to find the failed step
     */
    override fun getFailedStep(testResult: ITestResult): Step? {
        return testResult.throwable?.stackTrace.ifNotNullNotEmpty {
            val testClass = testResult.testClass.realClass
            var failedStep: Step? = null
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
                currentElement = it.getOrNull(++i) ?: break
                currentClass = Class.forName(currentElement.className)

            } while (isWithinTestClassScope(testClass, currentClass))

            return failedStep
        }
    }

    private inline fun <T, V> Array<T>?.ifNotNullNotEmpty(action: (Array<T>) -> V): V? {
        return if (!isNullOrEmpty()) {
            action(this)
        } else {
            null
        }
    }

    private fun isWithinTestClassScope(testClass: Class<*>, currentClass: Class<*>) =
        testClass == currentClass || currentClass.isAssignableFrom(testClass)

    private fun walkClassMethods(elementClass: Class<*>, expectedMethodName: String): Step? {
        return elementClass.declaredMethods.find { it.name == expectedMethodName }
            ?.getDeclaredAnnotation(Step::class.java)
            ?.apply {
                logger.trace {
                    "found ZephyrStep annotation: {annotation: {value: $value, description: $description}, " +
                            "class: ${elementClass.simpleName}, method: $expectedMethodName}"
                }
            }
    }
}