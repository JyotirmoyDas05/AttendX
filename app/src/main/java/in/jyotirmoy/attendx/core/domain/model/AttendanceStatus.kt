package `in`.jyotirmoy.attendx.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AttendanceStatus {
    PRESENT, ABSENT, UNMARKED
}