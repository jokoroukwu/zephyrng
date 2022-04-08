package io.github.jokoroukwu.zephyrng

import io.github.jokoroukwu.zephyrapi.ZephyrClient
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigLoaderImpl
import io.github.jokoroukwu.zephyrapi.publication.TestRunBase
import io.mockk.*
import org.testng.ISuite
import org.testng.annotations.AfterClass
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class TestNgZephyrAdapterTest {
    private val zephyrConfig = ZephyrConfigLoaderImpl.getZephyrConfig()
    private val testRunMock = mockk<TestRunBase>()
    private val suiteMock = mockk<ISuite>(relaxed = true)
    private val zephyrClientMock = mockk<ZephyrClient>(relaxed = true)
    private val testRunFactoryMock = mockk<TestRunFactory>()

    private lateinit var testNgZephyrAdapter: TestNgZephyrAdapter

    @BeforeMethod
    fun setUp() {
        testNgZephyrAdapter = TestNgZephyrAdapter(testRunFactory = testRunFactoryMock, zephyrClient = zephyrClientMock)
        every { testRunFactoryMock.getTestRuns(any()) } returns listOf(testRunMock, testRunMock)
    }

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearMocks(testRunFactoryMock, zephyrClientMock, suiteMock, testRunMock)
    }

    @AfterClass(alwaysRun = true)
    fun unMock() {
        unmockkObject(testRunFactoryMock, zephyrClientMock, suiteMock, testRunMock)
    }

    @Test
    fun `should call testRun factory with expected args`() {
        testNgZephyrAdapter.generateReport(null, mutableListOf(suiteMock, suiteMock), null)

        verify(exactly = 1) { testRunFactoryMock.getTestRuns(listOf(suiteMock, suiteMock)) }
    }

    @Test
    fun `should call zephyrClient with expected args`() {
        testNgZephyrAdapter.generateReport(null, mutableListOf(suiteMock, suiteMock), null)

        verify(exactly = 1) { zephyrClientMock.publishTestResults(listOf(testRunMock, testRunMock), zephyrConfig) }
    }
}
