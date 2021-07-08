package com.github.jokoroukwu.zephyrng.tests.gettestcaserequestsender

import com.github.jokoroukwu.zephyrng.config.ZephyrNgConfigLoaderImpl
import com.github.jokoroukwu.zephyrng.http.AbstractRequestSender.Companion.BASE_API_URL
import com.github.jokoroukwu.zephyrng.http.ZephyrException
import com.github.jokoroukwu.zephyrng.http.gettestcases.GetTestCasesRequestSender
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.AuthenticatedRequest
import com.github.kittinunf.fuel.core.extensions.authentication
import io.mockk.*
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.testng.annotations.AfterClass
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class GetTestCaseRequestSenderTest {

    private val keyOne = "key-one"
    private val keyTwo = "key-two"

    private val requestFactoryMock: RequestFactory.Convenience = mockk()
    private val zephyrConfig = ZephyrNgConfigLoaderImpl.zephyrNgConfig()
    private val jsonMock: Json = mockk()

    @BeforeClass
    fun beforeClass() {
        mockkStatic(Request::authentication)
    }

    @AfterClass(alwaysRun = true)
    fun afterClass() {
        clearStaticMockk(Request::class)
    }

    @AfterMethod(alwaysRun = true)
    fun setUp() {
        clearMocks(requestFactoryMock, jsonMock)
    }

    @Test(enabled = false)
    fun `should use expected URL`() {
        GetTestCasesRequestSender(config = zephyrConfig, requestFactory = requestFactoryMock).runCatching {
            getTestCasesRequest(listOf(keyOne, keyTwo))
        }
        val expectedURL = "${zephyrConfig.jiraUrl()}$BASE_API_URL/testcase/search?fields=id," +
                "key,projectId&maxResults=99999&query=testCase.key IN('$keyOne','$keyTwo')"

        verify(exactly = 1) { requestFactoryMock.get(expectedURL, any()) }
    }

    @Test(enabled = false)
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
    fun `should throw expected exception when connection failed`() {
        val exception = GetTestCasesRequestSender().runCatching {
            getTestCasesRequest(testCaseKeys = listOf(keyOne, keyTwo))
        }.exceptionOrNull()
        Assertions.assertThat(exception)
            .isInstanceOf(ZephyrException::class.java)
    }
}