package io.github.jokoroukwu.zephyrng.failedstepprovider

import io.github.jokoroukwu.zephyrapi.annotations.Step
import org.testng.ITestResult

interface FailedStepProvider {
    /**
     * Attempts to find the failed ZephyrStep using provided test result
     *
     * @return [Step] or null if failed step not found
     * @see FailedStepProviderImpl
     */
    fun getFailedStep(testResult: ITestResult): Step?
}