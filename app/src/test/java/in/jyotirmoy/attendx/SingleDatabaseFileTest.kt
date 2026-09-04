package `in`.jyotirmoy.attendx

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Guards the invariant that broke class notifications up to v2.8.5: DatabaseModule built
 * "attendance_app_db" while SubjectDatabase.getDatabase() built "subject_database", so every
 * BroadcastReceiver/Worker read an empty second database and resolved subject names to "Class".
 *
 * There must be exactly one Room builder for SubjectDatabase, using exactly one file name.
 */
class SingleDatabaseFileTest {

    @Test
    fun `SubjectDatabase is built exactly once, from one file name`() {
        val sources = File("src/main/java").walkTopDown().filter { it.extension == "kt" }
        val builders = sources.flatMap { file ->
            Regex("""databaseBuilder\(\s*[^)]*?SubjectDatabase::class\.java\s*,\s*"([^"]+)"""", RegexOption.DOT_MATCHES_ALL)
                .findAll(file.readText())
                .map { it.groupValues[1] }
        }.toList()

        assertEquals("expected one SubjectDatabase builder, found $builders", 1, builders.size)
        assertEquals("attendance_app_db", builders.single())
    }
}
