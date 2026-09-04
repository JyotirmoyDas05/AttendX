package `in`.jyotirmoy.attendx.core.presentation.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateList
import `in`.jyotirmoy.attendx.core.domain.model.ClassSchedule
import kotlinx.serialization.json.Json

/**
 * Savers that keep an unsaved timetable across a configuration change.
 *
 * The activity declares no `configChanges`, so rotating recreates it and any
 * plain `remember` is gone. That is survivable for a toggle; it is not
 * survivable for a list of classes the user just typed in, which is otherwise
 * silently discarded by turning the phone sideways.
 *
 * JSON rather than a `listSaver` of raw fields: everything a Bundle definitely
 * accepts, and it does not break the moment [ClassSchedule] gains a field.
 */
private val json = Json { ignoreUnknownKeys = true }

val ClassScheduleListSaver: Saver<List<ClassSchedule>, String> = Saver(
    save = { runCatching { json.encodeToString(it) }.getOrNull() },
    restore = { encoded ->
        runCatching { json.decodeFromString<List<ClassSchedule>>(encoded) }.getOrNull()
    }
)

val ClassScheduleStateListSaver: Saver<SnapshotStateList<ClassSchedule>, String> = Saver(
    save = { runCatching { json.encodeToString(it.toList()) }.getOrNull() },
    restore = { encoded ->
        runCatching {
            mutableStateListOf<ClassSchedule>().apply {
                addAll(json.decodeFromString<List<ClassSchedule>>(encoded))
            }
        }.getOrNull()
    }
)
