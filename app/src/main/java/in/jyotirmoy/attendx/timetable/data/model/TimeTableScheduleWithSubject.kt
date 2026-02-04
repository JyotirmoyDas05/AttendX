package `in`.jyotirmoy.attendx.timetable.data.model

import androidx.room.Embedded
import androidx.room.Relation
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity

/**
 * Joined data class that combines schedule with its associated subject.
 */
data class TimeTableScheduleWithSubject(
    @Embedded val schedule: TimeTableScheduleEntity,
    @Relation(
        parentColumn = "subjectId",
        entityColumn = "id"
    )
    val subject: SubjectEntity
)
