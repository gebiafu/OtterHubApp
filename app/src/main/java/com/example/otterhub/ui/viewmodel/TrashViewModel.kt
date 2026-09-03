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

class TrashViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepo = FileRepository()

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState

    init {
        loadTrashFiles()
    }

    fun loadTrashFiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // 直接按 trash 前缀拉取，避免正常文件/大量文件挤占前 100 条导致回收站显示不全。
            when (val result = fileRepo.getFileList(fileType = FileType.TRASH, limit = 1000)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        files = result.data.first,
                        isLoading = false
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

    fun restoreFile(key: String) {
        viewModelScope.launch {
            when (val res = fileRepo.restoreFromTrash(key)) {
                is Result.Success -> {
                    // 先本地移除，保证 UI 即时更新；再以服务端为准重新拉取校准。
                    _uiState.value = _uiState.value.copy(
                        files = _uiState.value.files.filter { it.key != key }
                    )
                    loadTrashFiles()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = res.message)
                }
            }
        }
    }

    fun deletePermanently(key: String) {
        viewModelScope.launch {
            when (val res = fileRepo.deleteFile(key)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        files = _uiState.value.files.filter { it.key != key }
                    )
                    loadTrashFiles()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = res.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TrashUiState(
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
