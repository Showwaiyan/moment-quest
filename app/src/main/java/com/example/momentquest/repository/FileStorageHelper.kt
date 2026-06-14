package com.example.momentquest.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileStorageHelper {
    private const val TAG = "FileStorageHelper"

    fun saveImageToInternalStorage(context: Context, uri: Uri, folderName: String): String? {
        Log.d(TAG, "Starting image save to internal storage folder: $folderName")
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            
            // Create target folder in internal filesDir
            val directory = File(context.filesDir, "photos/$folderName")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            // Create target file name
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val targetFile = File(directory, fileName)
            
            val outputStream = FileOutputStream(targetFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            
            inputStream.close()
            outputStream.flush()
            outputStream.close()
            
            Log.d(TAG, "Image successfully saved to path: ${targetFile.absolutePath}")
            // Return absolute path to store in SQLite
            targetFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image from URI: $uri", e)
            null
        }
    }

    fun deleteFile(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        Log.d(TAG, "Attempting to delete file at path: $path")
        return try {
            val file = File(path)
            if (file.exists()) {
                val deleted = file.delete()
                Log.d(TAG, "File deletion status at path $path: $deleted")
                deleted
            } else {
                Log.w(TAG, "File delete requested but file does not exist at path: $path")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while deleting file at path: $path", e)
            false
        }
    }
}

