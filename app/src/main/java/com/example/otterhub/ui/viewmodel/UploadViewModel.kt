package com.example.otterhub.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.otterhub.data.model.FileType
import com.example.otterhub.data.repository.Result
import com.example.otterhub.data.repository.UploadRepository
import com.example.otterhub.util.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class UploadViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val uploadRepo = UploadRepository()

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState

    fun uploadFile(fileUri: Uri, fileName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                currentFileName = fileName,
                progress = 0f,
                error = null
            )

            // Convert Uri to File
            val file = getFileFromUri(fileUri, fileName)
            if (file == null) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    error = "无法读取文件"
                )
                return@launch
            }

            val fileSize = file.length()
            val isLargeFile = fileSize > 5 * 1024 * 1024 // 5MB threshold

            if (isLargeFile) {
                uploadLargeFile(file, fileName, fileSize)
            } else {
                uploadSmallFile(file, fileName)
            }
        }
    }

    private suspend fun uploadSmallFile(file: File, fileName: String) {
        when (val result = uploadRepo.uploadSingleFile(file)) {
            is Result.Success -> {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    progress = 1f,
                    success = true
                )
                file.delete()
            }
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    error = result.message
                )
                file.delete()
            }
        }
    }

    private suspend fun uploadLargeFile(file: File, fileName: String, fileSize: Long) {
        _uiState.value = _uiState.value.copy(isChunked = true)

        val mimeType = FileUtils.getMimeType(fileName)
        val fileType = FileUtils.getFileTypeForUpload(mimeType).apiValue
        val chunkSize = 2 * 1024 * 1024 // 2MB chunks
        val totalChunks = Math.ceil(fileSize.toDouble() / chunkSize).toInt()

        // Initialize chunk upload
        when (val initResult = uploadRepo.initChunkUpload(
            fileType = fileType,
            fileName = fileName,
            fileSize = fileSize,
            totalChunks = totalChunks
        )) {
            is Result.Success -> {
                val uploadKey = initResult.data
                var uploadedChunks = 0

                // Upload chunks
                for (i in 0 until totalChunks) {
                    val start = i.toLong() * chunkSize
                    val end = minOf(start + chunkSize, fileSize)
                    val chunkFile = extractChunk(file, start, end, i)

                    when (uploadRepo.uploadChunk(uploadKey, i, chunkFile)) {
                        is Result.Success -> {
                            uploadedChunks++
                            _uiState.value = _uiState.value.copy(
                                progress = uploadedChunks.toFloat() / totalChunks
                            )
                        }
                        is Result.Error -> { chunkFile.delete(); return }
                    }
                    chunkFile.delete()
                }

                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    progress = 1f,
                    success = true
                )
                file.delete()
            }
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    error = initResult.message
                )
                file.delete()
            }
        }
    }

    private fun getFileFromUri(uri: Uri, fileName: String): File? {
        return try {
            val inputStream = app.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(app.cacheDir, fileName)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun extractChunk(file: File, start: Long, end: Long, index: Int): File {
        val chunkFile = File(app.cacheDir, "chunk_${index}_${file.name}")
        file.inputStream().use { input ->
            input.skip(start)
            chunkFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var remaining = end - start
                while (remaining > 0) {
                    val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    remaining -= read
                }
            }
        }
        return chunkFile
    }

    fun resetState() {
        _uiState.value = UploadUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class UploadUiState(
    val isUploading: Boolean = false,
    val currentFileName: String = "",
    val progress: Float = 0f,
    val error: String? = null,
    val success: Boolean = false,
    val isChunked: Boolean = false
)
