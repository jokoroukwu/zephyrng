package io.github.jokoroukwu.zephyrng.annotations

/**
 * Indicates that the test method is a candidate
 * for its result to be published to Zephyr
 *
 * @param value test case key as it appears in JIRA project
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class TestCaseKey(val value: String)