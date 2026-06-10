package com.example.momentquest.repository

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileStorageHelper {

    fun saveImageToInternalStorage(context: Context, uri: Uri, folderName: String): String? {
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
            
            // Return absolute path to store in SQLite
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteFile(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
