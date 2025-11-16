package com.studyconnect.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.studyconnect.app.ui.viewmodel.AuthEvent
import com.studyconnect.app.ui.viewmodel.AuthUiState
import kotlinx.coroutines.flow.Flow

@Composable
fun SignUpScreen(
    state: AuthUiState,
    snackbarHostState: SnackbarHostState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onSignUp: () -> Unit,
    onNavigateBack: () -> Unit,
    authEvents: Flow<AuthEvent>,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(authEvents) {
        authEvents.collect { event ->
            if (event is AuthEvent.Authenticated) {
                onAuthenticated()
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create your StudyConnect account",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Only @myuniversity.edu emails can join",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextField(
                value = state.displayName,
                onValueChange = onDisplayNameChanged,
                label = { Text("Display Name") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = state.email,
                onValueChange = onEmailChanged,
                label = { Text("University Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSignUp,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                } else {
                    Text("Sign Up")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onNavigateBack) {
                Text("Already have an account? Sign in")
            }
        }
    }
}
