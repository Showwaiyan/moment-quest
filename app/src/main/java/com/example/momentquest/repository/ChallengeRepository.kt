package com.example.momentquest.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.momentquest.model.Challenge
import com.example.momentquest.model.Memory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ChallengeRepository {

    suspend fun addChallenge(context: Context, challenge: Challenge): Unit = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        
        val id = if (challenge.id.isEmpty()) UUID.randomUUID().toString() else challenge.id
        
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, id)
            put(DatabaseHelper.COLUMN_CHALLENGE_TITLE, challenge.title)
            put(DatabaseHelper.COLUMN_CHALLENGE_CATEGORY, challenge.category)
            put(DatabaseHelper.COLUMN_CHALLENGE_DEADLINE, challenge.deadline)
            put(DatabaseHelper.COLUMN_CHALLENGE_STATUS, challenge.status)
            put(DatabaseHelper.COLUMN_CHALLENGE_CREATED_AT, challenge.createdAt)
        }
        
        db.insert(DatabaseHelper.TABLE_CHALLENGES, null, values)
        db.close()
    }

    suspend fun getChallenges(context: Context): List<Challenge> = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Challenge>()
        
        val cursor = db.query(
            DatabaseHelper.TABLE_CHALLENGES,
            null, null, null, null, null,
            "${DatabaseHelper.COLUMN_CHALLENGE_CREATED_AT} DESC"
        )
        
        while (cursor.moveToNext()) {
            val deadlineVal = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHALLENGE_DEADLINE))
            val deadline = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHALLENGE_DEADLINE))) null else deadlineVal
            
            val challenge = Challenge(
                id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHALLENGE_TITLE)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHALLENGE_CATEGORY)),
                deadline = deadline,
                status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHALLENGE_STATUS)),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHALLENGE_CREATED_AT))
            )
            list.add(challenge)
        }
        cursor.close()
        db.close()
        list
    }

    suspend fun updateChallenge(context: Context, challenge: Challenge): Unit = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CHALLENGE_TITLE, challenge.title)
            put(DatabaseHelper.COLUMN_CHALLENGE_CATEGORY, challenge.category)
            put(DatabaseHelper.COLUMN_CHALLENGE_DEADLINE, challenge.deadline)
            put(DatabaseHelper.COLUMN_CHALLENGE_STATUS, challenge.status)
            put(DatabaseHelper.COLUMN_CHALLENGE_CREATED_AT, challenge.createdAt)
        }
        
        db.update(
            DatabaseHelper.TABLE_CHALLENGES,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(challenge.id)
        )
        db.close()
    }

    suspend fun completeChallenge(context: Context, challengeId: String, memory: Memory): Unit = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        
        db.beginTransaction()
        try {
            // 1. Update challenge status to COMPLETED
            val challengeValues = ContentValues().apply {
                put(DatabaseHelper.COLUMN_CHALLENGE_STATUS, "COMPLETED")
            }
            db.update(
                DatabaseHelper.TABLE_CHALLENGES,
                challengeValues,
                "${DatabaseHelper.COLUMN_ID} = ?",
                arrayOf(challengeId)
            )
            
            // 2. Insert memory
            val memoryId = if (memory.id.isEmpty()) UUID.randomUUID().toString() else memory.id
            val memoryValues = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, memoryId)
                put(DatabaseHelper.COLUMN_MEMORY_CHALLENGE_ID, challengeId)
                put(DatabaseHelper.COLUMN_MEMORY_NOTES, memory.notes)
                put(DatabaseHelper.COLUMN_MEMORY_PHOTO_PATH, memory.photoUrl) // Storing local path in photoUrl field
                put(DatabaseHelper.COLUMN_LATITUDE, memory.latitude)
                put(DatabaseHelper.COLUMN_LONGITUDE, memory.longitude)
                put(DatabaseHelper.COLUMN_MEMORY_COMPLETED_AT, memory.completedAt)
            }
            db.insert(DatabaseHelper.TABLE_MEMORIES, null, memoryValues)
            
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    suspend fun getMemories(context: Context, challengeId: String): List<Memory> = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Memory>()
        
        val cursor = db.query(
            DatabaseHelper.TABLE_MEMORIES,
            null,
            "${DatabaseHelper.COLUMN_MEMORY_CHALLENGE_ID} = ?",
            arrayOf(challengeId),
            null, null,
            "${DatabaseHelper.COLUMN_MEMORY_COMPLETED_AT} DESC"
        )
        
        while (cursor.moveToNext()) {
            val latIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE)
            val lngIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE)
            val latitude = if (cursor.isNull(latIdx)) null else cursor.getDouble(latIdx)
            val longitude = if (cursor.isNull(lngIdx)) null else cursor.getDouble(lngIdx)
            
            val memory = Memory(
                id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                notes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MEMORY_NOTES)),
                photoUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MEMORY_PHOTO_PATH)),
                latitude = latitude,
                longitude = longitude,
                completedAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MEMORY_COMPLETED_AT))
            )
            list.add(memory)
        }
        cursor.close()
        db.close()
        list
    }

    suspend fun deleteMemory(context: Context, challengeId: String): Unit = withContext(Dispatchers.IO) {
        // Find memory photo paths to delete files first
        val memories = getMemories(context, challengeId)
        for (memory in memories) {
            FileStorageHelper.deleteFile(memory.photoUrl)
        }
        
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        
        db.beginTransaction()
        try {
            // 1. Delete memory
            db.delete(
                DatabaseHelper.TABLE_MEMORIES,
                "${DatabaseHelper.COLUMN_MEMORY_CHALLENGE_ID} = ?",
                arrayOf(challengeId)
            )
            
            // 2. Reset challenge status to PENDING
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_CHALLENGE_STATUS, "PENDING")
            }
            db.update(
                DatabaseHelper.TABLE_CHALLENGES,
                values,
                "${DatabaseHelper.COLUMN_ID} = ?",
                arrayOf(challengeId)
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    suspend fun deleteChallenge(context: Context, challengeId: String): Unit = withContext(Dispatchers.IO) {
        // Find memory photo paths to delete files first
        val memories = getMemories(context, challengeId)
        for (memory in memories) {
            FileStorageHelper.deleteFile(memory.photoUrl)
        }
        
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        db.delete(
            DatabaseHelper.TABLE_CHALLENGES,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(challengeId)
        )
        db.close()
    }
}
