package com.example.momentquest.repository

import android.content.ContentValues
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object MockDataHelper {
    private const val TAG = "MockDataHelper"
    private const val PREFS_NAME = "momentquest_prefs"
    private const val KEY_MOCK_INITIALIZED = "mock_data_initialized"

    suspend fun initializeMockDataIfNeeded(context: Context): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting mock data check and reset.")
        
        // 1. Copy images from assets to internal storage filesDir
        val memoriesFolder = File(context.filesDir, "photos/challenge_memories")
        val momentsFolder = File(context.filesDir, "photos/moments")

        val galleryFile = File(memoriesFolder, "national_gallery.png")
        val ramenFile = File(memoriesFolder, "ramen_spot.png")
        val sunsetFile = File(momentsFolder, "sunset_hike.png")
        val cafeFile = File(momentsFolder, "cozy_cafe.png")

        copyAssetToFile(context, "national_gallery.png", galleryFile)
        copyAssetToFile(context, "ramen_spot.png", ramenFile)
        copyAssetToFile(context, "sunset_hike.png", sunsetFile)
        copyAssetToFile(context, "cozy_cafe.png", cafeFile)

        // 2. Open Database
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase

        db.beginTransaction()
        try {
            // Delete existing mock records to reset them on every launch
            db.delete(DatabaseHelper.TABLE_CHALLENGES, "id LIKE 'mock_%'", null)
            db.delete(DatabaseHelper.TABLE_MEMORIES, "id LIKE 'mock_%'", null)
            db.delete(DatabaseHelper.TABLE_MOMENTS, "id LIKE 'mock_%'", null)

            val now = System.currentTimeMillis()

            // Insert Challenges
            // Challenge 1 (COMPLETED)
            val c1 = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, "mock_challenge_1")
                put(DatabaseHelper.COLUMN_CHALLENGE_TITLE, "Visit the National Gallery")
                put(DatabaseHelper.COLUMN_CHALLENGE_CATEGORY, "Travel")
                put(DatabaseHelper.COLUMN_CHALLENGE_DEADLINE, null as Long?)
                put(DatabaseHelper.COLUMN_CHALLENGE_STATUS, "COMPLETED")
                put(DatabaseHelper.COLUMN_CHALLENGE_CREATED_AT, now - 5 * 24 * 3600 * 1000L) // 5 days ago
            }
            db.insert(DatabaseHelper.TABLE_CHALLENGES, null, c1)

            // Challenge 2 (COMPLETED)
            val c2 = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, "mock_challenge_2")
                put(DatabaseHelper.COLUMN_CHALLENGE_TITLE, "Try a new ramen spot")
                put(DatabaseHelper.COLUMN_CHALLENGE_CATEGORY, "Food")
                put(DatabaseHelper.COLUMN_CHALLENGE_DEADLINE, null as Long?)
                put(DatabaseHelper.COLUMN_CHALLENGE_STATUS, "COMPLETED")
                put(DatabaseHelper.COLUMN_CHALLENGE_CREATED_AT, now - 3 * 24 * 3600 * 1000L) // 3 days ago
            }
            db.insert(DatabaseHelper.TABLE_CHALLENGES, null, c2)

            // Challenge 3 (PENDING)
            val c3 = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, "mock_challenge_3")
                put(DatabaseHelper.COLUMN_CHALLENGE_TITLE, "Complete 10km run")
                put(DatabaseHelper.COLUMN_CHALLENGE_CATEGORY, "Health")
                put(DatabaseHelper.COLUMN_CHALLENGE_DEADLINE, now + 7 * 24 * 3600 * 1000L) // 7 days from now
                put(DatabaseHelper.COLUMN_CHALLENGE_STATUS, "PENDING")
                put(DatabaseHelper.COLUMN_CHALLENGE_CREATED_AT, now - 1 * 24 * 3600 * 1000L) // 1 day ago
            }
            db.insert(DatabaseHelper.TABLE_CHALLENGES, null, c3)

            // Challenge 4 (PENDING)
            val c4 = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, "mock_challenge_4")
                put(DatabaseHelper.COLUMN_CHALLENGE_TITLE, "Read 'Clean Code' book")
                put(DatabaseHelper.COLUMN_CHALLENGE_CATEGORY, "Learning")
                put(DatabaseHelper.COLUMN_CHALLENGE_DEADLINE, now + 14 * 24 * 3600 * 1000L) // 14 days from now
                put(DatabaseHelper.COLUMN_CHALLENGE_STATUS, "PENDING")
                put(DatabaseHelper.COLUMN_CHALLENGE_CREATED_AT, now - 2 * 24 * 3600 * 1000L) // 2 days ago
            }
            db.insert(DatabaseHelper.TABLE_CHALLENGES, null, c4)

            // Challenge 5 (PENDING)
            val c5 = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, "mock_challenge_5")
                put(DatabaseHelper.COLUMN_CHALLENGE_TITLE, "Explore a new hiking trail")
                put(DatabaseHelper.COLUMN_CHALLENGE_CATEGORY, "Travel")
                put(DatabaseHelper.COLUMN_CHALLENGE_DEADLINE, null as Long?)
                put(DatabaseHelper.COLUMN_CHALLENGE_STATUS, "PENDING")
                put(DatabaseHelper.COLUMN_CHALLENGE_CREATED_AT, now - 4 * 24 * 3600 * 1000L) // 4 days ago
            }
            db.insert(DatabaseHelper.TABLE_CHALLENGES, null, c5)

            // Insert Memories linked to challenges
            val m1 = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, "mock_memory_1")
                put(DatabaseHelper.COLUMN_MEMORY_CHALLENGE_ID, "mock_challenge_1")
                put(DatabaseHelper.COLUMN_MEMORY_NOTES, "Spent the afternoon exploring the modern art exhibition. The architecture of the building is stunning!")
                put(DatabaseHelper.COLUMN_MEMORY_PHOTO_PATH, galleryFile.absolutePath)
                put(DatabaseHelper.COLUMN_LATITUDE, 3.1390)
                put(DatabaseHelper.COLUMN_LONGITUDE, 101.6869)
                put(DatabaseHelper.COLUMN_MEMORY_COMPLETED_AT, now - 4.5 * 24 * 3600 * 1000L)
            }
            db.insert(DatabaseHelper.TABLE_MEMORIES, null, m1)

            val m2 = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, "mock_memory_2")
                put(DatabaseHelper.COLUMN_MEMORY_CHALLENGE_ID, "mock_challenge_2")
                put(DatabaseHelper.COLUMN_MEMORY_NOTES, "Tried the spicy tonkotsu ramen at the new shop downtown. Highly recommended!")
                put(DatabaseHelper.COLUMN_MEMORY_PHOTO_PATH, ramenFile.absolutePath)
                put(DatabaseHelper.COLUMN_LATITUDE, 3.1412)
                put(DatabaseHelper.COLUMN_LONGITUDE, 101.6850)
                put(DatabaseHelper.COLUMN_MEMORY_COMPLETED_AT, now - 2.5 * 24 * 3600 * 1000L)
            }
            db.insert(DatabaseHelper.TABLE_MEMORIES, null, m2)

            // Insert Moments
            val mom1 = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, "mock_moment_1")
                put(DatabaseHelper.COLUMN_MOMENT_TITLE, "Beautiful sunset hike")
                put(DatabaseHelper.COLUMN_MOMENT_DESCRIPTION, "Caught the golden hour at the peak. The sky was painted in purple and orange hues.")
                put(DatabaseHelper.COLUMN_MOMENT_MOOD, "Grateful")
                put(DatabaseHelper.COLUMN_MOMENT_PHOTO_PATH, sunsetFile.absolutePath)
                put(DatabaseHelper.COLUMN_LATITUDE, 3.1550)
                put(DatabaseHelper.COLUMN_LONGITUDE, 101.6900)
                put(DatabaseHelper.COLUMN_MOMENT_CREATED_AT, now - 1 * 24 * 3600 * 1000L)
            }
            db.insert(DatabaseHelper.TABLE_MOMENTS, null, mom1)

            val mom2 = ContentValues().apply {
                put(DatabaseHelper.COLUMN_ID, "mock_moment_2")
                put(DatabaseHelper.COLUMN_MOMENT_TITLE, "Cozy cafe study session")
                put(DatabaseHelper.COLUMN_MOMENT_DESCRIPTION, "Found a quiet corner in a cozy cafe. Rain is pattering on the window, perfect vibe for writing code.")
                put(DatabaseHelper.COLUMN_MOMENT_MOOD, "Reflective")
                put(DatabaseHelper.COLUMN_MOMENT_PHOTO_PATH, cafeFile.absolutePath)
                put(DatabaseHelper.COLUMN_LATITUDE, 3.1200)
                put(DatabaseHelper.COLUMN_LONGITUDE, 101.6700)
                put(DatabaseHelper.COLUMN_MOMENT_CREATED_AT, now - 3.5 * 24 * 3600 * 1000L)
            }
            db.insert(DatabaseHelper.TABLE_MOMENTS, null, mom2)

            db.setTransactionSuccessful()
            Log.d(TAG, "Mock data check and reset successful.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error during mock data check and reset", e)
            false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    private fun copyAssetToFile(context: Context, assetName: String, targetFile: File): Boolean {
        return try {
            val parent = targetFile.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }
            context.assets.open(assetName).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }
            Log.d(TAG, "Copied asset $assetName to ${targetFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error copying asset $assetName", e)
            false
        }
    }
}
