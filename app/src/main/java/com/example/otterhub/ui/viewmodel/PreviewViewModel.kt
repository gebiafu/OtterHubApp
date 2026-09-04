package com.example.otterhub.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.otterhub.data.model.FileItem
import com.example.otterhub.data.model.FileType
import com.example.otterhub.data.repository.FileRepository
import com.example.otterhub.data.repository.Result
import com.example.otterhub.util.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PreviewViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepo = FileRepository()

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState

    private val _videoUri = MutableStateFlow<Uri?>(null)
    val videoUri: StateFlow<Uri?> = _videoUri

    private val _audioUri = MutableStateFlow<Uri?>(null)
    val audioUri: StateFlow<Uri?> = _audioUri

    fun loadFileInfo(key: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 仅按该文件的类型前缀查询，limit 拉满，避免文件不在首页时找不到。
            val fileType = FileUtils.getFileTypeFromKey(key)
            when (val result = fileRepo.getFileList(fileType = fileType, limit = 1000)) {
                is Result.Success -> {
                    val allFiles = result.data.first
                    val file = allFiles.find { it.key == key }
                    
                    // 如果是图片类型，保存所有图片列表用于滑动切换
                    val imageList = if (fileType == FileType.IMAGE) {
                        allFiles.filter { it.fileType == FileType.IMAGE }
                    } else emptyList()
                    
                    if (file != null) {
                        _uiState.value = _uiState.value.copy(
                            file = file,
                            allImages = imageList,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "文件不存在"
                        )
                    }
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

    fun getDownloadUrl(key: String): String {
        return fileRepo.getFileDownloadUrl(key)
    }

    fun getRawUrl(key: String): String {
        return fileRepo.getFileUrl(key)
    }

    fun getThumbUrl(key: String): String {
        return fileRepo.getFileThumbUrl(key)
    }

    fun toggleLike(key: String) {
        viewModelScope.launch {
            when (fileRepo.toggleLike(key)) {
                is Result.Success -> {
                    val file = _uiState.value.file
                    if (file != null) {
                        _uiState.value = _uiState.value.copy(
                            file = file.copy(
                                metadata = file.metadata.copy(liked = !file.metadata.liked)
                            )
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun deleteFile(key: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (fileRepo.deleteFile(key)) {
                is Result.Success -> onSuccess()
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = "删除失败")
                }
                else -> {}
            }
        }
    }

    fun moveToTrash(key: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (fileRepo.moveToTrash(key)) {
                is Result.Success -> onSuccess()
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = "操作失败")
                }
                else -> {}
            }
        }
    }
}

data class PreviewUiState(
    val file: FileItem? = null,
    val allImages: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
