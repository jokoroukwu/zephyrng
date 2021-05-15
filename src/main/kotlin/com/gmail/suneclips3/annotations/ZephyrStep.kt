package com.gmail.suneclips3.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ZephyrStep(val value: Int, val description: String = "")
