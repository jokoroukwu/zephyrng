package com.github.jokoroukwu.zephyrng.http.testresultstatus

import kotlinx.serialization.Serializable

@Serializable
class SerializableTestResultStatusItem(val id: Int, val name: TestResultStatus)