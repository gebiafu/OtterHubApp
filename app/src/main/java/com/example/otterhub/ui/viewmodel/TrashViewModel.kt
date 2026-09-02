package com.example.otterhub.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.otterhub.data.model.FileItem
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
            when (val result = fileRepo.getFileList(limit = 100)) {
                is Result.Success -> {
                    val trashFiles = result.data.first.filter {
                        it.key.startsWith("trash:")
                    }
                    _uiState.value = _uiState.value.copy(
                        files = trashFiles,
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
            when (fileRepo.restoreFromTrash(key)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        files = _uiState.value.files.filter { it.key != key }
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = "恢复失败")
                }
                else -> {}
            }
        }
    }

    fun deletePermanently(key: String) {
        viewModelScope.launch {
            when (fileRepo.deleteFile(key)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        files = _uiState.value.files.filter { it.key != key }
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = "删除失败")
                }
                else -> {}
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
