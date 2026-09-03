package com.example.otterhub.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.otterhub.data.model.FileItem
import com.example.otterhub.data.model.FileType
import com.example.otterhub.data.repository.FileRepository
import com.example.otterhub.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FileViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepo = FileRepository()

    private val _uiState = MutableStateFlow(FileUiState())
    val uiState: StateFlow<FileUiState> = _uiState

    private var allFiles = mutableListOf<FileItem>()
    private var currentFileType: FileType? = null

    fun loadFiles(fileType: FileType? = null, refresh: Boolean = false) {
        viewModelScope.launch {
            // 如果类型改变，强制刷新
            if (fileType != currentFileType) {
                currentFileType = fileType
                allFiles.clear()
                _uiState.value = _uiState.value.copy(cursor = null, hasMore = true, files = emptyList())
            }
            
            if (refresh) {
                currentFileType = fileType
                allFiles.clear()
                _uiState.value = _uiState.value.copy(cursor = null, hasMore = true, files = emptyList())
            }

            val currentCursor = _uiState.value.cursor
            if (!refresh && !_uiState.value.hasMore) return@launch

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = fileRepo.getFileList(fileType = fileType, cursor = currentCursor)) {
                is Result.Success -> {
                    val (files, nextCursor) = result.data
                    allFiles.addAll(files.filter(::isVisibleFile))
                    _uiState.value = _uiState.value.copy(
                        files = allFiles.toList(),
                        isLoading = false,
                        cursor = nextCursor,
                        hasMore = nextCursor != null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = fileRepo.getFileList(limit = 100)) {
                is Result.Success -> {
                    val favorites = result.data.first.filter { it.metadata.liked && isVisibleFile(it) }
                    _uiState.value = _uiState.value.copy(
                        files = favorites,
                        isLoading = false,
                        hasMore = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun toggleLike(key: String) {
        viewModelScope.launch {
            when (fileRepo.toggleLike(key)) {
                is Result.Success -> {
                    allFiles = allFiles.map {
                        if (it.key == key) {
                            it.copy(metadata = it.metadata.copy(liked = !it.metadata.liked))
                        } else it
                    }.toMutableList()
                    _uiState.value = _uiState.value.copy(files = allFiles.toList())
                }
                else -> {}
            }
        }
    }

    fun deleteFile(key: String) {
        viewModelScope.launch {
            when (fileRepo.deleteFile(key)) {
                is Result.Success -> {
                    allFiles.removeAll { it.key == key }
                    _uiState.value = _uiState.value.copy(files = allFiles.toList())
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = "删除失败")
                }
                else -> {}
            }
        }
    }

    fun moveToTrash(key: String) {
        viewModelScope.launch {
            when (fileRepo.moveToTrash(key)) {
                is Result.Success -> {
                    allFiles.removeAll { it.key == key }
                    _uiState.value = _uiState.value.copy(files = allFiles.toList())
                }
                else -> {}
            }
        }
    }

    fun searchFiles(query: String) {
        val filtered = if (query.isBlank()) {
            allFiles
        } else {
            allFiles.filter {
                it.fileName.contains(query, ignoreCase = true) ||
                (it.metadata.desc?.contains(query, ignoreCase = true) == true)
            }
        }
        _uiState.value = _uiState.value.copy(files = filtered, searchQuery = query)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /** 过滤回收站文件、尚未上传完成的分片文件和空文件（0字节），避免它们出现在正常列表中。 */
    private fun isVisibleFile(file: FileItem): Boolean {
        if (file.key.startsWith("trash:")) return false
        if (file.fileSize == 0L) return false
        val chunkInfo = file.metadata.chunkInfo
        if (chunkInfo != null && chunkInfo.uploadedIndices.size != chunkInfo.total) return false
        return true
    }
}

data class FileUiState(
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val cursor: String? = null,
    val hasMore: Boolean = true,
    val searchQuery: String = ""
)
