package com.gmail.johnokoroukwu.zephyrng.tests.endtoend.util

import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.matching.MatchResult
import com.github.tomakehurst.wiremock.matching.ValueMatcher

object CustomRequestMatcher {

    fun urlStartsWith(value: String) =
        ValueMatcher<Request> { request -> MatchResult.of(request.url.startsWith(value)) }

    fun urlEndsWith(value: String) =
        ValueMatcher<Request> { request -> MatchResult.of(request.url.endsWith(value)) }

}