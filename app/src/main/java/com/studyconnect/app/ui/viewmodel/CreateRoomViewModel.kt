package com.studyconnect.app.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val CAMPUS_DOMAIN = "@myuniversity.edu"

data class CreateRoomUiState(
    val title: String = "",
    val memberEmails: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val roomCreated: Boolean = false
)

class CreateRoomViewModel(
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRoomUiState())
    val uiState: StateFlow<CreateRoomUiState> = _uiState.asStateFlow()

    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun updateMemberInput(value: String) {
        _uiState.update { it.copy(memberEmails = value) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeRoomCreated() {
        _uiState.update { it.copy(roomCreated = false) }
    }

    fun createRoom() {
        val currentState = _uiState.value
        val parsedEmails = parseEmails(currentState.memberEmails)
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Room title cannot be empty") }
            return
        }
        val invalidEmails = parsedEmails.filterNot { Patterns.EMAIL_ADDRESS.matcher(it).matches() }
        if (invalidEmails.isNotEmpty()) {
            _uiState.update { it.copy(errorMessage = "Invalid email(s): ${invalidEmails.joinToString()}") }
            return
        }
        val nonCampusEmails = parsedEmails.filterNot { it.endsWith(CAMPUS_DOMAIN) }
        if (nonCampusEmails.isNotEmpty()) {
            _uiState.update { it.copy(errorMessage = "Only $CAMPUS_DOMAIN addresses are allowed") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, roomCreated = false) }
            try {
                repository.createChatRoom(currentState.title.trim(), parsedEmails)
                _uiState.update {
                    it.copy(
                        title = "",
                        memberEmails = "",
                        isSaving = false,
                        roomCreated = true
                    )
                }
            } catch (throwable: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.localizedMessage ?: "Unable to create chat room"
                    )
                }
            }
        }
    }

    private fun parseEmails(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.split(',', ';', '\n', '\r', '\t', ' ')
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}
