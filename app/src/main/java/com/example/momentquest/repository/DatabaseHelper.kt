package com.example.momentquest.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "momentquest.db"
        private const val DATABASE_VERSION = 1

        // Tables
        const val TABLE_CHALLENGES = "challenges"
        const val TABLE_MOMENTS = "moments"
        const val TABLE_MEMORIES = "memories"

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

        db.execSQL(createChallengesTable)
        db.execSQL(createMomentsTable)
        db.execSQL(createMemoriesTable)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MEMORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MOMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHALLENGES")
        onCreate(db)
    }
}
