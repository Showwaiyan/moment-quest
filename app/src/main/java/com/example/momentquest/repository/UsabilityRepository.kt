package com.example.momentquest.repository

import android.content.ContentValues
import android.content.Context
import com.example.momentquest.model.UsabilityResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class UsabilityRepository {

    suspend fun addResult(context: Context, result: UsabilityResult): Unit = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase

        val id = if (result.id.isEmpty()) UUID.randomUUID().toString() else result.id

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, id)
            put(DatabaseHelper.COLUMN_RESULT_PARTICIPANT, result.participantName)
            put(DatabaseHelper.COLUMN_RESULT_VARIANT, result.variant)
            put(DatabaseHelper.COLUMN_RESULT_TIME_MS, result.timeMs)
            put(DatabaseHelper.COLUMN_RESULT_EASE_RATING, result.easeRating)
            put(DatabaseHelper.COLUMN_RESULT_ERROR_COUNT, result.errorCount)
            put(DatabaseHelper.COLUMN_RESULT_TIMESTAMP, result.timestamp)
        }

        db.insert(DatabaseHelper.TABLE_USABILITY_RESULTS, null, values)
        db.close()
    }

    suspend fun getResults(context: Context): List<UsabilityResult> = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.readableDatabase
        val list = mutableListOf<UsabilityResult>()

        val cursor = db.query(
            DatabaseHelper.TABLE_USABILITY_RESULTS,
            null, null, null, null, null,
            "${DatabaseHelper.COLUMN_RESULT_TIMESTAMP} ASC"
        )

        while (cursor.moveToNext()) {
            val result = UsabilityResult(
                id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                participantName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESULT_PARTICIPANT)),
                variant = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESULT_VARIANT)),
                timeMs = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESULT_TIME_MS)),
                easeRating = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESULT_EASE_RATING)),
                errorCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESULT_ERROR_COUNT)),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESULT_TIMESTAMP))
            )
            list.add(result)
        }
        cursor.close()
        db.close()
        list
    }

    suspend fun clearResults(context: Context): Unit = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_USABILITY_RESULTS, null, null)
        db.close()
    }

    suspend fun insertMockResults(context: Context): Unit = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase

        // Clear existing first
        db.delete(DatabaseHelper.TABLE_USABILITY_RESULTS, null, null)

        val now = System.currentTimeMillis()
        val mockData = listOf(
            // Participant 1 (P1)
            UsabilityResult(UUID.randomUUID().toString(), "P1", "A", 18200L, 4, 1, now - 600000),
            UsabilityResult(UUID.randomUUID().toString(), "P1", "B", 12400L, 5, 0, now - 540000),
            // Participant 2 (P2)
            UsabilityResult(UUID.randomUUID().toString(), "P2", "A", 24500L, 3, 2, now - 480000),
            UsabilityResult(UUID.randomUUID().toString(), "P2", "B", 15100L, 4, 1, now - 420000),
            // Participant 3 (P3)
            UsabilityResult(UUID.randomUUID().toString(), "P3", "A", 16800L, 4, 0, now - 360000),
            UsabilityResult(UUID.randomUUID().toString(), "P3", "B", 11900L, 5, 0, now - 300000),
            // Participant 4 (P4)
            UsabilityResult(UUID.randomUUID().toString(), "P4", "A", 21300L, 4, 1, now - 240000),
            UsabilityResult(UUID.randomUUID().toString(), "P4", "B", 14500L, 5, 0, now - 180000)
        )

        for (result in mockData) {
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, result.id)
                put(DatabaseHelper.COLUMN_RESULT_PARTICIPANT, result.participantName)
                put(DatabaseHelper.COLUMN_RESULT_VARIANT, result.variant)
                put(DatabaseHelper.COLUMN_RESULT_TIME_MS, result.timeMs)
                put(DatabaseHelper.COLUMN_RESULT_EASE_RATING, result.easeRating)
                put(DatabaseHelper.COLUMN_RESULT_ERROR_COUNT, result.errorCount)
                put(DatabaseHelper.COLUMN_RESULT_TIMESTAMP, result.timestamp)
            }
            db.insert(DatabaseHelper.TABLE_USABILITY_RESULTS, null, values)
        }
        db.close()
    }
}
