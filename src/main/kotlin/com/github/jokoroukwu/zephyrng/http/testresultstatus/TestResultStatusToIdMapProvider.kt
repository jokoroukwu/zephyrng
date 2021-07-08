package com.github.jokoroukwu.zephyrng.http.testresultstatus

import com.github.jokoroukwu.zephyrng.http.ZephyrException
import java.util.*

class TestResultStatusToIdMapProvider(
    private val getTestResultStatusesRequestSender: GetTestResultStatusesRequestSender = GetTestResultStatusesRequestSender()
) {

    fun getTestResultStatusToIdMap(projectId: Long): Map<TestResultStatus, Long> {
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
