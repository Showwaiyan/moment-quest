package com.example.momentquest.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageRepository {

    suspend fun uploadPhoto(context: Context, photoUri: Uri, pathPrefix: String): String = withContext(Dispatchers.IO) {
        FileStorageHelper.saveImageToInternalStorage(context, photoUri, pathPrefix)
            ?: throw Exception("Failed to save image locally")
    }
}
