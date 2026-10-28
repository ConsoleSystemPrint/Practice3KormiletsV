package com.example.practice3kormiletsv.data.local

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.practice3kormiletsv.domain.repository.AvatarRepository
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class LocalAvatarRepository(private val context: Context) : AvatarRepository {
    private val authority = "${context.packageName}.files"
    private val images = File(context.filesDir, "profile_images")
    private val captures = File(context.cacheDir, "profile_capture")

    override suspend fun importPhoto(uri: String): String = withContext(Dispatchers.IO) {
        ensureDirectory(images)
        val target = File(images, "${UUID.randomUUID()}.jpg")
        try {
            val source = context.contentResolver.openInputStream(Uri.parse(uri)) ?: throw IOException("Cannot open photo")
            source.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > 25L * 1024 * 1024) throw IOException("Photo too large")
                        output.write(buffer, 0, count)
                    }
                }
            }
            val dimensions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(target.absolutePath, dimensions)
            if (dimensions.outWidth <= 0 || dimensions.outHeight <= 0) throw IOException("Invalid photo")
            FileProvider.getUriForFile(context, authority, target).toString()
        } catch (error: Exception) {
            target.delete()
            throw error
        }
    }

    override suspend fun createCameraCapture(): String = withContext(Dispatchers.IO) {
        ensureDirectory(captures)
        val target = File(captures, "${UUID.randomUUID()}.jpg")
        if (!target.createNewFile()) throw IOException("Cannot create camera file")
        FileProvider.getUriForFile(context, authority, target).toString()
    }

    override suspend fun deleteOwnedPhoto(uri: String?) = withContext(Dispatchers.IO) {
        if (uri != null) {
            val parsed = Uri.parse(uri)
            if (parsed.authority == authority && parsed.pathSegments.size == 2) {
                val directory = when (parsed.pathSegments[0]) {
                    "profile_images" -> images
                    "profile_capture" -> captures
                    else -> null
                }
                if (directory != null) {
                    val file = File(directory, parsed.lastPathSegment.orEmpty())
                    if (file.canonicalFile.parentFile == directory.canonicalFile) file.delete()
                }
            }
        }
        Unit
    }

    private fun ensureDirectory(directory: File) {
        if (!directory.exists() && !directory.mkdirs()) throw IOException("Cannot create profile directory")
    }
}
