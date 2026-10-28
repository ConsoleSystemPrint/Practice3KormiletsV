package com.example.practice3kormiletsv.ui.profile

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PdfUiState(
    val name: String = "Резюме",
    val page: Int = 0,
    val pageCount: Int = 0,
    val bitmap: Bitmap? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class PdfViewerViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val uri = savedStateHandle.get<String>("documentUri").orEmpty()
    private val mutableState = MutableStateFlow(PdfUiState(name = savedStateHandle["documentName"] ?: "Резюме"))
    val state = mutableState.asStateFlow()
    private var pageJob: Job? = null

    init { loadPage(0) }

    fun retry() = loadPage(state.value.page)
    fun previous() { if (state.value.page > 0) loadPage(state.value.page - 1) }
    fun next() { if (state.value.page + 1 < state.value.pageCount) loadPage(state.value.page + 1) }

    private fun loadPage(index: Int) {
        pageJob?.cancel()
        mutableState.update { it.copy(page = index, bitmap = null, isLoading = true, error = null) }
        pageJob = viewModelScope.launch {
            try {
                val rendered = withContext(Dispatchers.IO) {
                    val descriptor = getApplication<Application>().contentResolver.openFileDescriptor(Uri.parse(uri), "r") ?: throw IOException("Cannot open PDF")
                    descriptor.use { file ->
                        PdfRenderer(file).use { renderer ->
                            renderer.openPage(index).use { page ->
                                val width = 1200
                                val height = (width.toLong() * page.height / page.width).toInt().coerceIn(1, 4000)
                                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                bitmap.eraseColor(Color.WHITE)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                bitmap to renderer.pageCount
                            }
                        }
                    }
                }
                mutableState.update { it.copy(bitmap = rendered.first, pageCount = rendered.second, isLoading = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(isLoading = false, error = "Не удалось открыть PDF. Возможно, файл повреждён или защищён паролем.") }
            }
        }
    }
}
