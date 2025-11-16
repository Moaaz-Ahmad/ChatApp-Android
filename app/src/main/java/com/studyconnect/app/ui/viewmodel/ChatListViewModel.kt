package com.studyconnect.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.model.ChatRoom
import com.studyconnect.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatListUiState(
    val rooms: List<ChatRoom> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class ChatListViewModel(
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        observeRooms()
    }

    private fun observeRooms() {
        viewModelScope.launch {
            val userId = repository.currentUserId()
            if (userId.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        rooms = emptyList(),
                        isLoading = false,
                        errorMessage = "You must be signed in to view rooms"
                    )
                }
                return@launch
            }
            repository.chatRoomsFlow(userId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            errorMessage = throwable.localizedMessage ?: "Unable to load chat rooms",
                            isLoading = false
                        )
                    }
                }
                .collect { rooms ->
                    _uiState.update {
                        it.copy(rooms = rooms, isLoading = false, errorMessage = null)
                    }
                }
        }
    }

    fun signOut() {
        repository.signOut()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
