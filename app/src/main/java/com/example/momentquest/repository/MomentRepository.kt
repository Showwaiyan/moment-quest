package com.example.momentquest.repository

import android.content.ContentValues
import android.content.Context
import com.example.momentquest.model.Moment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class MomentRepository {

    suspend fun addMoment(context: Context, moment: Moment): Unit = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        
        val id = if (moment.id.isEmpty()) UUID.randomUUID().toString() else moment.id
        
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, id)
            put(DatabaseHelper.COLUMN_MOMENT_TITLE, moment.title)
            put(DatabaseHelper.COLUMN_MOMENT_DESCRIPTION, moment.description)
            put(DatabaseHelper.COLUMN_MOMENT_MOOD, moment.mood)
            put(DatabaseHelper.COLUMN_MOMENT_PHOTO_PATH, moment.photoUrl) // Storing local path in photoUrl field
            put(DatabaseHelper.COLUMN_LATITUDE, moment.latitude)
            put(DatabaseHelper.COLUMN_LONGITUDE, moment.longitude)
            put(DatabaseHelper.COLUMN_MOMENT_CREATED_AT, moment.createdAt)
        }
        
        db.insert(DatabaseHelper.TABLE_MOMENTS, null, values)
        db.close()
    }

    suspend fun getMoments(context: Context): List<Moment> = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Moment>()
        
        val cursor = db.query(
            DatabaseHelper.TABLE_MOMENTS,
            null, null, null, null, null,
            "${DatabaseHelper.COLUMN_MOMENT_CREATED_AT} DESC"
        )
        
        while (cursor.moveToNext()) {
            val latIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE)
            val lngIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE)
            val latitude = if (cursor.isNull(latIdx)) null else cursor.getDouble(latIdx)
            val longitude = if (cursor.isNull(lngIdx)) null else cursor.getDouble(lngIdx)
            
            val moment = Moment(
                id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MOMENT_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MOMENT_DESCRIPTION)),
                mood = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MOMENT_MOOD)),
                photoUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MOMENT_PHOTO_PATH)),
                latitude = latitude,
                longitude = longitude,
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MOMENT_CREATED_AT))
            )
            list.add(moment)
        }
        cursor.close()
        db.close()
        list
    }

    suspend fun deleteMoment(context: Context, momentId: String): Unit = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        
        // Find photo path to delete file
        val cursor = db.query(
            DatabaseHelper.TABLE_MOMENTS,
            arrayOf(DatabaseHelper.COLUMN_MOMENT_PHOTO_PATH),
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(momentId),
            null, null, null
        )
        
        if (cursor.moveToFirst()) {
            val photoPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MOMENT_PHOTO_PATH))
            FileStorageHelper.deleteFile(photoPath)
        }
        cursor.close()
        
        db.delete(
            DatabaseHelper.TABLE_MOMENTS,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(momentId)
        )
        db.close()
    }
}
