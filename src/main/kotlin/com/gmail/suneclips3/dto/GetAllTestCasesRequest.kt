package com.gmail.suneclips3.dto


import kotlinx.serialization.Serializable

@Serializable
data class GetAllTestCasesRequest(
    val component: Component?,
    val createdOn: String?,
    val customFields: CustomFields?,
    val estimatedTime: Int?,
    val folder: Folder?,
    val id: Int?,
    val key: String?,
    val labels: List<String>?,
    val name: String?,
    val objective: String?,
    val owner: Owner?,
    val precondition: String?,
    val priority: Priority?,
    val project: Project?,
    val status: Status?,
    val testScript: TestScript?
)