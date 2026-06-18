package com.example.momentquest.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "momentquest.db"
        private const val DATABASE_VERSION = 2

        // Tables
        const val TABLE_CHALLENGES = "challenges"
        const val TABLE_MOMENTS = "moments"
        const val TABLE_MEMORIES = "memories"
        const val TABLE_USABILITY_RESULTS = "usability_results"

        // Common columns
        const val COLUMN_ID = "id"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"

        // Challenges Table Columns
        const val COLUMN_CHALLENGE_TITLE = "title"
        const val COLUMN_CHALLENGE_CATEGORY = "category"
        const val COLUMN_CHALLENGE_DEADLINE = "deadline"
        const val COLUMN_CHALLENGE_STATUS = "status"
        const val COLUMN_CHALLENGE_CREATED_AT = "createdAt"

        // Moments Table Columns
        const val COLUMN_MOMENT_TITLE = "title"
        const val COLUMN_MOMENT_DESCRIPTION = "description"
        const val COLUMN_MOMENT_MOOD = "mood"
        const val COLUMN_MOMENT_PHOTO_PATH = "photoPath"
        const val COLUMN_MOMENT_CREATED_AT = "createdAt"

        // Memories Table Columns
        const val COLUMN_MEMORY_CHALLENGE_ID = "challengeId"
        const val COLUMN_MEMORY_NOTES = "notes"
        const val COLUMN_MEMORY_PHOTO_PATH = "photoPath"
        const val COLUMN_MEMORY_COMPLETED_AT = "completedAt"

        // Usability Results Table Columns
        const val COLUMN_RESULT_PARTICIPANT = "participantName"
        const val COLUMN_RESULT_VARIANT = "variant"
        const val COLUMN_RESULT_TIME_MS = "timeMs"
        const val COLUMN_RESULT_EASE_RATING = "easeRating"
        const val COLUMN_RESULT_ERROR_COUNT = "errorCount"
        const val COLUMN_RESULT_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createChallengesTable = """
            CREATE TABLE $TABLE_CHALLENGES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_CHALLENGE_TITLE TEXT NOT NULL,
                $COLUMN_CHALLENGE_CATEGORY TEXT NOT NULL,
                $COLUMN_CHALLENGE_DEADLINE INTEGER,
                $COLUMN_CHALLENGE_STATUS TEXT NOT NULL,
                $COLUMN_CHALLENGE_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        val createMomentsTable = """
            CREATE TABLE $TABLE_MOMENTS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_MOMENT_TITLE TEXT NOT NULL,
                $COLUMN_MOMENT_DESCRIPTION TEXT NOT NULL,
                $COLUMN_MOMENT_MOOD TEXT NOT NULL,
                $COLUMN_MOMENT_PHOTO_PATH TEXT,
                $COLUMN_LATITUDE REAL,
                $COLUMN_LONGITUDE REAL,
                $COLUMN_MOMENT_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        val createMemoriesTable = """
            CREATE TABLE $TABLE_MEMORIES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_MEMORY_CHALLENGE_ID TEXT NOT NULL,
                $COLUMN_MEMORY_NOTES TEXT NOT NULL,
                $COLUMN_MEMORY_PHOTO_PATH TEXT,
                $COLUMN_LATITUDE REAL,
                $COLUMN_LONGITUDE REAL,
                $COLUMN_MEMORY_COMPLETED_AT INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_MEMORY_CHALLENGE_ID) REFERENCES $TABLE_CHALLENGES($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createUsabilityResultsTable = """
            CREATE TABLE $TABLE_USABILITY_RESULTS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_RESULT_PARTICIPANT TEXT NOT NULL,
                $COLUMN_RESULT_VARIANT TEXT NOT NULL,
                $COLUMN_RESULT_TIME_MS INTEGER NOT NULL,
                $COLUMN_RESULT_EASE_RATING INTEGER NOT NULL,
                $COLUMN_RESULT_ERROR_COUNT INTEGER NOT NULL,
                $COLUMN_RESULT_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createChallengesTable)
        db.execSQL(createMomentsTable)
        db.execSQL(createMemoriesTable)
        db.execSQL(createUsabilityResultsTable)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val createUsabilityResultsTable = """
                CREATE TABLE $TABLE_USABILITY_RESULTS (
                    $COLUMN_ID TEXT PRIMARY KEY,
                    $COLUMN_RESULT_PARTICIPANT TEXT NOT NULL,
                    $COLUMN_RESULT_VARIANT TEXT NOT NULL,
                    $COLUMN_RESULT_TIME_MS INTEGER NOT NULL,
                    $COLUMN_RESULT_EASE_RATING INTEGER NOT NULL,
                    $COLUMN_RESULT_ERROR_COUNT INTEGER NOT NULL,
                    $COLUMN_RESULT_TIMESTAMP INTEGER NOT NULL
                )
            """.trimIndent()
            db.execSQL(createUsabilityResultsTable)
        } else {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_MEMORIES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_MOMENTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CHALLENGES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USABILITY_RESULTS")
            onCreate(db)
        }
    }
}
