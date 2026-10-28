package com.example.practice3kormiletsv.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.practice3kormiletsv.domain.model.ResumeDocument
import com.example.practice3kormiletsv.domain.repository.ResumeRepository
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class NetworkResumeRepository(private val context: Context, private val client: OkHttpClient) : ResumeRepository {
    override suspend fun download(url: String, onProgress: (Int?) -> Unit): ResumeDocument = withContext(Dispatchers.IO) {
        val directory = File(context.filesDir, "profile_documents")
        if (!directory.exists() && !directory.mkdirs()) throw IOException("Cannot create document directory")
        var target: File? = null
        try {
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Document HTTP ${response.code}")
                val body = response.body ?: throw IOException("Empty document response")
                val contentType = body.contentType()?.toString()?.substringBefore(';')
                if (contentType == "text/html") throw IOException("URL points to a web page")
                val originalName = Uri.parse(response.request.url.toString()).lastPathSegment.orEmpty()
                val extension = originalName.substringAfterLast('.', "").lowercase().takeIf { it.matches(Regex("[a-z0-9]{1,8}")) }
                val mime = contentType?.takeUnless { it == "application/octet-stream" }
                    ?: extension?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
                    ?: "application/octet-stream"
                val suffix = if (mime == "application/pdf") "pdf" else extension ?: MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "bin"
                val name = if (originalName.contains('.') && originalName.length <= 100) originalName.replace(Regex("[^\\p{L}\\p{N}._ -]"), "_") else "Resume.$suffix"
                val file = File(directory, "${UUID.randomUUID()}_$name")
                target = file
                val length = body.contentLength()
                if (length > MAX_BYTES) throw IOException("Document too large")
                onProgress(if (length > 0) 0 else null)
                body.byteStream().use { input ->
                    file.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var total = 0L
                        var previousProgress = -1
                        while (true) {
                            ensureActive()
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            if (total > MAX_BYTES) throw IOException("Document too large")
                            output.write(buffer, 0, count)
                            if (length > 0) {
                                val progress = (total * 100 / length).toInt().coerceIn(0, 100)
                                if (progress != previousProgress) { onProgress(progress); previousProgress = progress }
                            }
                        }
                        if (total == 0L) throw IOException("Empty document")
                    }
                }
                ResumeDocument(FileProvider.getUriForFile(context, "${context.packageName}.files", file, name).toString(), name, mime)
            }
        } catch (error: Exception) {
            target?.delete()
            throw error
        }
    }

    private companion object { const val MAX_BYTES = 30L * 1024 * 1024 }
}
