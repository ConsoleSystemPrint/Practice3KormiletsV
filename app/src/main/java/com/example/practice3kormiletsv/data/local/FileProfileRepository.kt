package com.example.practice3kormiletsv.data.local

import android.util.AtomicFile
import com.example.practice3kormiletsv.domain.model.UserProfile
import com.example.practice3kormiletsv.domain.repository.ProfileRepository
import com.google.gson.Gson
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class FileProfileRepository(file: File) : ProfileRepository {
    private val file = AtomicFile(file)
    private val gson = Gson()
    private val changes = MutableStateFlow(0L)
    private val mutex = Mutex()

    override fun observeProfile() = changes.map {
        mutex.withLock {
            try {
                file.openRead().bufferedReader().use { reader ->
                    val record = gson.fromJson(reader, ProfileRecord::class.java) ?: throw IOException("Empty profile")
                    UserProfile(record.fullName.orEmpty(), record.avatarUri, record.resumeUrl.orEmpty())
                }
            } catch (_: FileNotFoundException) {
                UserProfile()
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun save(profile: UserProfile) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val output = file.startWrite()
            try {
                val record = ProfileRecord(profile.fullName, profile.avatarUri, profile.resumeUrl)
                output.write(gson.toJson(record).toByteArray(Charsets.UTF_8))
                file.finishWrite(output)
                changes.value += 1
            } catch (error: Exception) {
                file.failWrite(output)
                throw error
            }
        }
    }

    private data class ProfileRecord(val fullName: String?, val avatarUri: String?, val resumeUrl: String?)
}
