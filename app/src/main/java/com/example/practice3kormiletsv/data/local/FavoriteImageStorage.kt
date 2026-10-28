package com.example.practice3kormiletsv.data.local

import java.io.File
import java.io.IOException
import java.util.UUID
import okhttp3.OkHttpClient
import okhttp3.Request

class FavoriteImageStorage(private val directory: File, private val client: OkHttpClient) {
    fun download(url: String): String? {
        if (url.isBlank()) return null
        if (!directory.exists() && !directory.mkdirs()) throw IOException("Cannot create image directory")
        val file = File(directory, "${UUID.randomUUID()}.jpg")
        try {
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Image HTTP ${response.code}")
                val body = response.body ?: throw IOException("Empty image response")
                body.byteStream().use { input -> file.outputStream().use { input.copyTo(it) } }
                if (file.length() == 0L) throw IOException("Empty image file")
            }
            return file.absolutePath
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }

    fun delete(path: String?) {
        if (path != null) {
            val file = File(path)
            if (file.parentFile == directory) file.delete()
        }
    }
}
