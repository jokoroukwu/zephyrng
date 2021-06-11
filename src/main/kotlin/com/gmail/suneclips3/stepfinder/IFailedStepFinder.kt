package com.gmail.suneclips3.stepfinder

import com.gmail.suneclips3.annotations.ZephyrStep
import org.testng.ITestResult

interface IFailedStepFinder {
    fun findFailedStep(testResult: ITestResult): ZephyrStep?
}