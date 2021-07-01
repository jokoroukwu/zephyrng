package com.github.jokoroukwu.zephyrng.tests.testngzephyradapter

import com.github.jokoroukwu.zephyrng.TestNgZephyrAdapter
import com.github.jokoroukwu.zephyrng.http.AsyncZephyrPublisher
import com.github.jokoroukwu.zephyrng.http.TestNgZephyrSuite
import com.github.jokoroukwu.zephyrng.http.gettestcases.ZephyrProjectWithTestCases
import com.github.jokoroukwu.zephyrng.http.gettestcases.ZephyrTestCaseFetcher
import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatus
import com.github.jokoroukwu.zephyrng.http.testresultstatus.TestResultStatusToIdMapProvider
import com.github.jokoroukwu.zephyrng.suitetestcaseidmerger.TestCaseIdInjector
import com.github.jokoroukwu.zephyrng.suitetestcaseidmerger.TestCasesIdInjectResult
import com.github.jokoroukwu.zephyrng.suitewithresultsfactory.TestNgZephyrSuiteFactory
import io.mockk.*
import org.testng.ISuite
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.*

class TestNgZephyrAdapterTest {
    private val projectKey = "project-key"
    private val zephyrPublisherMock = mockk<AsyncZephyrPublisher>(relaxed = true)
    private val testCaseFetcherMock = mockk<ZephyrTestCaseFetcher>(relaxed = true)
    private val suiteWithResultsFactoryMock = mockk<TestNgZephyrSuiteFactory>(relaxed = true)
    private val testResultToIdMapperMock = mockk<TestResultStatusToIdMapProvider>(relaxed = true)
    private val suiteResultToIdMapperMock = mockk<TestResultStatusToIdMapProvider>(relaxed = true)
    private val testCaseIdInjectorMock = mockk<TestCaseIdInjector>(relaxed = true)
    private val suiteMock = mockk<ISuite>(relaxed = true)

    private lateinit var testNgZephyrAdapter: TestNgZephyrAdapter

    @BeforeMethod
    fun setUp() {
        testNgZephyrAdapter = TestNgZephyrAdapter(
            projectKey = projectKey,
            zephyrPublisher = zephyrPublisherMock,
            zephyrTestCaseFetcher = testCaseFetcherMock,
            testNgZephyrSuiteFactory = suiteWithResultsFactoryMock,
            testResultStatusToIdMapProvider = testResultToIdMapperMock,
            suiteTestCaseIdInjector = testCaseIdInjectorMock
        )
    }

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearMocks(
            zephyrPublisherMock, testCaseFetcherMock, suiteResultToIdMapperMock,
            testResultToIdMapperMock, suiteWithResultsFactoryMock, suiteMock,
            testCaseIdInjectorMock
        )
    }

    @Test
    fun `should return when filtered suites empty`() {
        val testCaseKey = "some-key"
        val testSuiteWithResultMock = mockk<TestNgZephyrSuite> {
            every { testCasesWithDataSetResults } returns listOf(mockk {
                every { key } returns testCaseKey
            })
        }
        every { suiteWithResultsFactoryMock.get(any()) } returns testSuiteWithResultMock

        val keyToIdMap = hashMapOf(testCaseKey to 0)
        val projectId = 1
        every { testCaseFetcherMock.fetchProjectWithTestCases(any()) } returns
                ZephyrProjectWithTestCases(projectId, keyToIdMap)

        val suitesWithResults = listOf(testSuiteWithResultMock)
        every { testCaseIdInjectorMock.injectTestCaseIds(any(), any()) } returns
                TestCasesIdInjectResult(suitesWithResults, emptySet(), emptySet())

        val testResultStatusToIdMap = EnumMap<TestResultStatus, Int>(TestResultStatus::class.java).apply {
            put(TestResultStatus.PASS, 0)
        }
        every { testResultToIdMapperMock.getTestResultStatusToIdMap(any()) } returns testResultStatusToIdMap


        testNgZephyrAdapter.runCatching {
            generateReport(
                null, mutableListOf(suiteMock), null
            )
        }

        verify(exactly = 1) { testResultToIdMapperMock.getTestResultStatusToIdMap(projectId) }
        verify(exactly = 1) {
            zephyrPublisherMock.submitResults(projectId, suitesWithResults, testResultStatusToIdMap)
        }

    }


    @Test
    fun `should call testResultStatusToIdMapProvider and publisher`() {
        val testCaseKey = "some-key"
        val testSuiteWithResultMock = mockk<TestNgZephyrSuite> {
            every { testCasesWithDataSetResults } returns listOf(mockk {
                every { key } returns testCaseKey
            })
        }
        every { suiteWithResultsFactoryMock.get(any()) } returns testSuiteWithResultMock

        val keyToIdMap = hashMapOf(testCaseKey to 0)
        val projectId = 1
        every { testCaseFetcherMock.fetchProjectWithTestCases(any()) } returns
                ZephyrProjectWithTestCases(projectId, keyToIdMap)

        val suitesWithResults = listOf(testSuiteWithResultMock)
        every { testCaseIdInjectorMock.injectTestCaseIds(any(), any()) } returns
                TestCasesIdInjectResult(suitesWithResults, emptySet(), emptySet())

        val testResultStatusToIdMap = EnumMap<TestResultStatus, Int>(TestResultStatus::class.java).apply {
            put(TestResultStatus.PASS, 0)
        }
        every { testResultToIdMapperMock.getTestResultStatusToIdMap(any()) } returns testResultStatusToIdMap


        testNgZephyrAdapter.runCatching {
            generateReport(
                null, mutableListOf(suiteMock), null
            )
        }

        verify(exactly = 1) { testResultToIdMapperMock.getTestResultStatusToIdMap(projectId) }
        verify(exactly = 1) {
            zephyrPublisherMock.submitResults(projectId, suitesWithResults, testResultStatusToIdMap)
        }

    }

    @Test
    fun `should call suiteTestCaseToIdMerger`() {
        val testCaseKey = "some-key"
        val testSuiteWithResultMock = mockk<TestNgZephyrSuite> {
            every { testCasesWithDataSetResults } returns listOf(mockk {
                every { key } returns testCaseKey
            })
        }
        every { suiteWithResultsFactoryMock.get(any()) } returns testSuiteWithResultMock

        val keyToIdMap = hashMapOf(testCaseKey to 0)
        every { testCaseFetcherMock.fetchProjectWithTestCases(any()) } returns mockk {
            every { testCaseKeyToIdMap } returns keyToIdMap
        }
        testNgZephyrAdapter.runCatching {
            generateReport(
                null, mutableListOf(suiteMock), null
            )
        }
        verify(exactly = 1) {
            testCaseIdInjectorMock.injectTestCaseIds(
                match { it.contains(testSuiteWithResultMock) },
                keyToIdMap
            )
        }
    }

    @Test
    fun `should call suiteWithResultsFactory with expected args`() {
        val suiteMockTwo = mockk<ISuite>(relaxed = true)
        testNgZephyrAdapter.runCatching {
            generateReport(
                null, mutableListOf(suiteMock, suiteMockTwo), null
            )
        }
        verify(exactly = 1) { suiteWithResultsFactoryMock.get(suiteMock) }
        verify(exactly = 1) { suiteWithResultsFactoryMock.get(suiteMockTwo) }
    }

    @Test
    fun `should return when no cases fetched from Zephyr`() {
        every { suiteWithResultsFactoryMock.get(any()) } returns mockk {
            every { testCasesWithDataSetResults } returns listOf(mockk {
                every { key } returns "test-key"
            })
        }

        every { testCaseFetcherMock.fetchProjectWithTestCases(any()) } returns null
        testNgZephyrAdapter.generateReport(
            null, mutableListOf(suiteMock), null
        )
        verify(exactly = 1) { testCaseFetcherMock.fetchProjectWithTestCases(any()) }
        verify {
            listOf(
                zephyrPublisherMock,
                testResultToIdMapperMock,
                testCaseIdInjectorMock
            ) wasNot called
        }
    }

    @Test
    fun `should return when no test case keys found`() {
        every { suiteWithResultsFactoryMock.get(any()) } returns mockk {
            every { testCasesWithDataSetResults } returns emptyList()
        }
        testNgZephyrAdapter.generateReport(
            null, mutableListOf(suiteMock), null
        )
        verify {
            listOf(
                zephyrPublisherMock,
                testCaseFetcherMock,
                testResultToIdMapperMock,
                testCaseIdInjectorMock
            ) wasNot called
        }
    }

    @DataProvider
    private fun testCasesWithResultsProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf()
        )
    }
}
