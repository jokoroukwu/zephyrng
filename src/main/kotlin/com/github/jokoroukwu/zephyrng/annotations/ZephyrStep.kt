package com.github.jokoroukwu.zephyrng.annotations

/**
 * Indicates that the method corresponds to a particular
 * step of Zephyr test result
 *
 * @param value index of the corresponding Zephyr test result step (zero based)
 * @param description step description (optional)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ZephyrStep(val value: Int, val description: String = "<none>")
