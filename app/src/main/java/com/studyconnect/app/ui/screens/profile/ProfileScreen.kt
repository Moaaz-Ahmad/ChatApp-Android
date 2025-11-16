package com.studyconnect.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studyconnect.app.ui.viewmodel.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onDisplayNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onErrorDismissed: () -> Unit,
    onSuccessDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismissed()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSuccessDismissed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(26.dp))
                }
            }
            Text(
                text = "Email",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = state.email.ifBlank { "Unavailable" },
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.displayName,
                onValueChange = onDisplayNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Display Name") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSave,
                enabled = !state.isSaving && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
}
