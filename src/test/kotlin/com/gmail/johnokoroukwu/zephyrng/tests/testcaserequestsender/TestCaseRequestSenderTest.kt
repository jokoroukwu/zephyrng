package com.gmail.johnokoroukwu.zephyrng.tests.testcaserequestsender

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.AuthenticatedRequest
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import com.gmail.johnokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.gmail.johnokoroukwu.zephyrng.http.AbstractRequestSender.Companion.BASE_API_URL
import com.gmail.johnokoroukwu.zephyrng.http.ZephyrResponseDeserializer
import com.gmail.johnokoroukwu.zephyrng.http.gettestcases.GetTestCasesRequestSender
import io.mockk.*
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.testng.annotations.*

class TestCaseRequestSenderTest {
    private val jiraURL = "http://jira.com"

    private val keyOne = "key-one"
    private val keyTwo = "key-two"

    private lateinit var requestFactoryMock: RequestFactory.Convenience
    private val zephyrConfig = ZephyrNgConfigLoaderImpl.zephyrNgConfig()
    private lateinit var json: Json

    @BeforeClass
    fun beforeClass() {
        mockkStatic(Request::authentication)
    }

    @AfterClass(alwaysRun = true)
    fun afterClass() {
        clearStaticMockk(Request::class)
    }

    @BeforeMethod
    fun setUp() {
        json = mockk()
        requestFactoryMock = mockk(relaxed = true)
    }

    @Test
    fun `should use expected URL`() {
        GetTestCasesRequestSender(config = zephyrConfig, requestFactory = requestFactoryMock).runCatching {
            getTestCasesRequest(listOf(keyOne, keyTwo))
        }
        val expectedURL = "${zephyrConfig.jiraUrl()}$BASE_API_URL/testcase/search?fields=id," +
                "key,projectId&maxResults=99999&query=testCase.key IN('$keyOne','$keyTwo')"

        verify(exactly = 1) { requestFactoryMock.get(expectedURL, any()) }
    }

    @Test
    fun `should use expected credentials`() {
        val authMock = mockk<AuthenticatedRequest>(relaxed = true)
        every { requestFactoryMock.get(any<String>()) } returns mockk {
            every { authentication() } returns authMock
        }

        GetTestCasesRequestSender(config = zephyrConfig, requestFactory = requestFactoryMock).runCatching {
            getTestCasesRequest(listOf(keyTwo, keyTwo))
        }
        verify(exactly = 1) { authMock.basic(zephyrConfig.username(), zephyrConfig.password()) }
    }

    @Test
    fun `should throw expected exception with expected cause when request fails`() {
        val expectedCause = RuntimeException()
        every { requestFactoryMock.get(any<String>()) } throws expectedCause

        val throwable = GetTestCasesRequestSender(config = zephyrConfig, requestFactory = requestFactoryMock)
            .runCatching { getTestCasesRequest(listOf(keyOne, keyTwo)) }
            .exceptionOrNull()

        Assertions.assertThat(throwable)
            .isNotNull
            .extracting { exception -> exception.cause }
            .isEqualTo(expectedCause)
    }

    @Test
    fun `should throw expected exception when status code validation failed`() {
        every { requestFactoryMock.get(any<String>()) } returns mockk<AuthenticatedRequest> {

            every { authentication() } returns this
            every { basic(any(), any()) } returns this
            every { response(ZephyrResponseDeserializer) } returns mockk {

                every { third.get() } returns mockk {
                    every { statusCode } returns 500 //    any error status code
                }
            }
        }

        Assertions.assertThatThrownBy {
            GetTestCasesRequestSender(zephyrConfig, requestFactory = requestFactoryMock)
                .getTestCasesRequest(listOf("test-key"))
        }.isNotNull
    }

    @DataProvider
    private fun mockProvider(): Array<Array<Any>> {
        return arrayOf(

        )
    }
}