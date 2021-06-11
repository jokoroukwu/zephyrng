package com.gmail.suneclips3.http.testresultstatus

import com.gmail.suneclips3.connectionconfig.ZephyrConfigLoader
import com.gmail.suneclips3.http.ZephyrException
import java.util.*

class TestResultToIdMapper(
    private val getTestResultStatusesRequestSender: GetTestResultStatusesRequestSender = GetTestResultStatusesRequestSender(
        ZephyrConfigLoader.connectionConfig()
    )
) {

    fun getTestResultStatusToIdMap(projectId: Int): Map<TestResultStatus, Int> {
        return getTestResultStatusesRequestSender.getTestResultStatusesRequest(projectId)
            .associateTo(EnumMap(TestResultStatus::class.java)) { it.name to it.id }
            .also { map ->
                with(EnumSet.allOf(TestResultStatus::class.java)) {
                    removeAll(map.keys)
                    takeIf(Set<TestResultStatus>::isEmpty)
                        ?: throw ZephyrException("no id mappings for test result statuses $this: {actual_mappings: $map}")
                }
            }.run(Collections::unmodifiableMap)
    }
}
