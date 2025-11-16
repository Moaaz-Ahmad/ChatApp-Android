package com.studyconnect.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.model.Message
import com.studyconnect.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatRoomUiState(
    val roomTitle: String = "",
    val messages: List<Message> = emptyList(),
    val messageInput: String = "",
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val currentUserId: String? = null
)

class ChatRoomViewModel(
    private val roomId: String,
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatRoomUiState(currentUserId = repository.currentUserId()))
    val uiState: StateFlow<ChatRoomUiState> = _uiState.asStateFlow()

    init {
        observeRoomDetails()
        observeMessages()
    }

    private fun observeRoomDetails() {
        viewModelScope.launch {
            repository.chatRoomFlow(roomId).collect { room ->
                _uiState.update {
                    it.copy(
                        roomTitle = room?.title ?: "StudyConnect Room"
                    )
                }
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            repository.messagesFlow(roomId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            errorMessage = throwable.localizedMessage ?: "Unable to load messages",
                            isLoading = false
                        )
                    }
                }
                .collect { messages ->
                    _uiState.update {
                        it.copy(messages = messages, isLoading = false, errorMessage = null)
                    }
                }
        }
    }

    fun updateMessageInput(value: String) {
        _uiState.update { it.copy(messageInput = value) }
    }

    fun sendMessage() {
        val messageBody = uiState.value.messageInput
        if (messageBody.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            runCatching {
                repository.sendMessage(roomId, messageBody)
            }.onSuccess {
                _uiState.update { it.copy(messageInput = "", isSending = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = throwable.localizedMessage ?: "Unable to send message"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(private val roomId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChatRoomViewModel(roomId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
