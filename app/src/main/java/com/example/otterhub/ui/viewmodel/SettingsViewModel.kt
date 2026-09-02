package com.example.otterhub.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.otterhub.data.api.RetrofitClient
import com.example.otterhub.data.local.PrefsManager
import com.example.otterhub.data.repository.AuthRepository
import com.example.otterhub.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PrefsManager(application)
    private val authRepo = AuthRepository()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            prefs.baseUrl.collect { _uiState.value = _uiState.value.copy(baseUrl = it) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepo.logout()
            prefs.clearAuth()
            _uiState.value = _uiState.value.copy(isLoading = false, isLoggedOut = true)
        }
    }

    fun resetSetup() {
        viewModelScope.launch {
            prefs.clearAll()
            _uiState.value = _uiState.value.copy(isReset = true)
        }
    }
}

data class SettingsUiState(
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isReset: Boolean = false
)
