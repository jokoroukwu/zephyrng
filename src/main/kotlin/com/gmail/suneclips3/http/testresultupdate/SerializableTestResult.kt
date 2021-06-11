package com.gmail.suneclips3.http.testresultupdate

import kotlinx.serialization.Serializable

@Serializable
class SerializableTestResult(val id: Int, val testResultStatusId: Int, val comment: String) {
}