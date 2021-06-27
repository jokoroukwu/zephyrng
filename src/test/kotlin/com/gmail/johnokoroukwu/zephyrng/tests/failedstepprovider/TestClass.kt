package com.gmail.johnokoroukwu.zephyrng.tests.failedstepprovider

import com.gmail.johnokoroukwu.zephyrng.annotations.ZephyrStep

const val STEP_INDEX = 1
const val NESTED_STEP_INDEX = 2
const val OVERRIDDEN_STEP_INDEX = 3
const val OTHER_CLASS_STEP_INDEX = 4

class TestClass : AncestorClass() {
    private val otherClass = OtherClass()

    @ZephyrStep(NESTED_STEP_INDEX)
    fun callNestedStepMethod() = nestedStepMethod()

    @ZephyrStep(OVERRIDDEN_STEP_INDEX)
    public override fun overriddenWithStep(): Unit = super.overriddenWithStep()

    @ZephyrStep(STEP_INDEX)
    fun methodWithStep(): Unit = throw RuntimeException()


    fun callOtherClass() = otherClass.otherClassStepMethod()

}

open class AncestorClass {

    @ZephyrStep(-1)
    private fun throwingMethod(): Unit = throw RuntimeException()

    protected open fun nestedStepMethod() = throwingMethod()

    @ZephyrStep(-1)
    protected open fun overriddenWithStep(): Unit = throw  RuntimeException()
}

class OtherClass {

    @ZephyrStep(OTHER_CLASS_STEP_INDEX)
    fun otherClassStepMethod(): Unit = throw RuntimeException()
}