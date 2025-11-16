package com.studyconnect.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            repository.currentUserFlow().collect { user ->
                _uiState.update {
                    it.copy(
                        displayName = user?.displayName ?: "",
                        email = user?.email.orEmpty(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateDisplayName(value: String) {
        _uiState.update { it.copy(displayName = value) }
    }

    fun saveProfile() {
        val desiredName = uiState.value.displayName.trim()
        if (desiredName.isBlank()) {
            emitError("Display name cannot be empty")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            runCatching { repository.updateDisplayName(desiredName) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, successMessage = "Profile updated") }
                }
                .onFailure { throwable ->
                    emitError(throwable.localizedMessage ?: "Unable to update profile")
                    _uiState.update { it.copy(isSaving = false) }
                }
        }
    }

    private fun emitError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
