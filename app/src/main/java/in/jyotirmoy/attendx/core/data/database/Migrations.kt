package `in`.jyotirmoy.attendx.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE subjects ADD COLUMN savedMonth INTEGER")
        db.execSQL("ALTER TABLE subjects ADD COLUMN savedYear INTEGER")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE subjects ADD COLUMN subjectCode TEXT")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE subjects ADD COLUMN targetPercentage REAL NOT NULL DEFAULT 75.0")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE subjects ADD COLUMN histogramLabel TEXT")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE subjects ADD COLUMN isTargetSet INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS class_schedules (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                subjectId INTEGER NOT NULL,
                dayOfWeek INTEGER NOT NULL,
                startTime TEXT NOT NULL,
                endTime TEXT NOT NULL,
                location TEXT,
                isEnabled INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS class_schedule (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                subjectId INTEGER NOT NULL,
                dayOfWeek INTEGER NOT NULL,
                startTime INTEGER NOT NULL,
                endTime INTEGER NOT NULL,
                room TEXT,
                professor TEXT,
                classType TEXT NOT NULL DEFAULT 'Lecture',
                isActive INTEGER NOT NULL DEFAULT 1,
                weekPattern TEXT NOT NULL DEFAULT 'all',
                notes TEXT,
                colorHex TEXT,
                FOREIGN KEY(subjectId) REFERENCES subjects(id) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS index_class_schedule_subjectId ON class_schedule(subjectId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_class_schedule_dayOfWeek_startTime ON class_schedule(dayOfWeek, startTime)")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS peer_comparison_cache (
                groupKey TEXT NOT NULL PRIMARY KEY,
                averageAttendance REAL NOT NULL,
                stdDev REAL NOT NULL,
                sampleSize INTEGER NOT NULL,
                userPercentile REAL NOT NULL,
                fetchedAt INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS subject_tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                subjectId INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                dueDate INTEGER,
                priority INTEGER NOT NULL DEFAULT 0,
                isNote INTEGER NOT NULL DEFAULT 0,
                noteType TEXT,
                reminderMinutes INTEGER,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(subjectId) REFERENCES subjects(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_subject_tasks_subjectId ON subject_tasks(subjectId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS course_materials (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                subjectId INTEGER NOT NULL,
                title TEXT NOT NULL,
                type TEXT NOT NULL,
                uriOrUrl TEXT NOT NULL,
                mimeType TEXT,
                addedAt INTEGER NOT NULL,
                FOREIGN KEY(subjectId) REFERENCES subjects(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_course_materials_subjectId ON course_materials(subjectId)")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE subject_tasks ADD COLUMN noteType TEXT")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE subject_tasks ADD COLUMN reminderMinutes INTEGER")
        } catch (_: Exception) {}
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Drop old peer comparison cache table — replaced by ephemeral QR session model
        db.execSQL("DROP TABLE IF EXISTS peer_comparison_cache")
    }
}





