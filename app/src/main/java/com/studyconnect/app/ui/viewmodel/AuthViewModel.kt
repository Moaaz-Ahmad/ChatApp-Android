package com.studyconnect.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.studyconnect.app.data.remote.FirestoreRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val RequiredDomain = "@myuniversity.edu"

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AuthEvent {
    data object Authenticated : AuthEvent
}

class AuthViewModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authEvents = Channel<AuthEvent>(Channel.BUFFERED)
    val authEvents = _authEvents.receiveAsFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun updateDisplayName(displayName: String) {
        _uiState.update { it.copy(displayName = displayName) }
    }

    fun signIn() {
        val email = uiState.value.email.trim()
        val password = uiState.value.password
        if (!isDomainValid(email)) {
            emitError("Use your university email ($RequiredDomain)")
            return
        }
        if (password.length < 6) {
            emitError("Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            setLoading(true)
            runCatching {
                auth.signInWithEmailAndPassword(email, password).await()
            }.onSuccess {
                _authEvents.send(AuthEvent.Authenticated)
                setLoading(false)
            }.onFailure { throwable ->
                emitError(throwable.localizedMessage ?: "Unable to sign in")
            }
        }
    }

    fun signUp() {
        val email = uiState.value.email.trim()
        val password = uiState.value.password
        val displayName = uiState.value.displayName.trim()
        if (!isDomainValid(email)) {
            emitError("Use your university email ($RequiredDomain)")
            return
        }
        if (displayName.isBlank()) {
            emitError("Display name is required")
            return
        }
        if (password.length < 6) {
            emitError("Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            setLoading(true)
            runCatching {
                auth.createUserWithEmailAndPassword(email, password).await()
                repository.ensureUserDocument(displayName)
            }.onSuccess {
                _authEvents.send(AuthEvent.Authenticated)
                setLoading(false)
            }.onFailure { throwable ->
                emitError(throwable.localizedMessage ?: "Unable to create account")
            }
        }
    }

    private fun isDomainValid(email: String) = email.endsWith(RequiredDomain, ignoreCase = true)

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading, errorMessage = null) }
    }

    private fun emitError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}
