package io.github.jokoroukwu.zephyrng.failedstepprovider

import io.github.jokoroukwu.zephyrng.annotations.ZephyrStep
import org.testng.ITestResult

interface FailedStepProvider {
    /**
     * Attempts to find the failed ZephyrStep using provided test result
     *
     * @return [ZephyrStep] or null if failed step not found
     * @see FailedStepProviderImpl
     */
    fun getFailedStep(testResult: ITestResult): ZephyrStep?
}