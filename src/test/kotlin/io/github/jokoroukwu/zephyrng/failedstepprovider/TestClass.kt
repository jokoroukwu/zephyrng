package io.github.jokoroukwu.zephyrng.failedstepprovider

import io.github.jokoroukwu.zephyrapi.annotations.Step

const val STEP_INDEX = 1
const val NESTED_STEP_INDEX = 2
const val OVERRIDDEN_STEP_INDEX = 3
const val OTHER_CLASS_STEP_INDEX = 4

class TestClass : AncestorClass() {
    private val otherClass = OtherClass()

    @Step(NESTED_STEP_INDEX)
    fun callNestedStepMethod() = nestedStepMethod()

    @Step(OVERRIDDEN_STEP_INDEX)
    public override fun overriddenWithStep(): Unit = super.overriddenWithStep()

    @Step(STEP_INDEX)
    fun methodWithStep(): Unit = throw RuntimeException()


    fun callOtherClass() = otherClass.otherClassStepMethod()

}

open class AncestorClass {

    @Step(-1)
    private fun throwingMethod(): Unit = throw RuntimeException()

    protected open fun nestedStepMethod() = throwingMethod()

    @Step(-1)
    protected open fun overriddenWithStep(): Unit = throw  RuntimeException()
}

class OtherClass {

    @Step(OTHER_CLASS_STEP_INDEX)
    fun otherClassStepMethod(): Unit = throw RuntimeException()
}