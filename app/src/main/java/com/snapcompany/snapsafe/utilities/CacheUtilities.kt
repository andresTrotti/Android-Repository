package com.snapcompany.snapsafe.utilities

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream

class CacheUtilities {

    private fun createCacheDirectory(context: Context, uniqueName: String): File {
        // Check if external storage is available and mounted
        val cachePath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.externalCacheDir?.path
        } else {
            context.cacheDir.path
        }

        // Create the cache directory
        return File(cachePath + File.separator + uniqueName).apply {
            mkdirs()
        }
    }



    fun saveImageToCache(context: Context, imageUri: Uri, uniqueName: String) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val cacheDirectory = createCacheDirectory(context, "images")
            val cacheFile = File(cacheDirectory, uniqueName)

            inputStream?.use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., IOException)
            Log.e("Cache", "Error saving image to cache", e)
        }
    }



    /*fun saveImage(context: Context, uniqueName: String, fileToSave: File?) {
        // Create the cache directory
        val cacheDirectory = createCacheDirectory(context, "images")

        // Create the cache file
        val cacheFile = File(cacheDirectory, uniqueName)



        // Copy the file to the cache
        if(fileToSave != null) {
            val inputStream = fileToSave.inputStream()
            val outputStream = FileOutputStream(cacheFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        }
        else{
            Log.e("CacheUtilities", "the file is null")
        }
    }*/

    fun loadImage(context: Context, uniqueName: String): Uri? {
        // Create the cache directory
        val cacheDirectory = createCacheDirectory(context, "images")

        if (uniqueName.isNotEmpty()) {
            // Create the cache file
            val cacheFile = File(cacheDirectory, uniqueName)

            // Check if the cache file exists
            if (cacheFile.exists()) {
                return cacheFile.toUri()
            }
        }

        // Return null if the cache file does not exist
        return null
    }
}