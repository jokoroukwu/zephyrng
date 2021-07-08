package io.github.jokoroukwu.zephyrng.http

/**
 * An intermediate container that encapsulates information about TestNG suite,
 * TestNG test results associated with that suite along with test cases keys
 * and test cases ids (prefetched from JIRA server)
 */
data class TestNgZephyrSuite(
    val plannedStartDate: String,
    val plannedEndDate: String,
    val name: String,
    val testCasesWithDataSetResults: List<io.github.jokoroukwu.zephyrng.TestCaseWithTestNgResults>
)
