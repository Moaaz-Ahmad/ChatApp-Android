package com.studyconnect.app.ui.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.studyconnect.app.ui.viewmodel.CreateRoomUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    state: CreateRoomUiState,
    snackbarHostState: SnackbarHostState,
    onTitleChanged: (String) -> Unit,
    onMemberEmailsChanged: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onBack: () -> Unit,
    onErrorDismissed: () -> Unit,
    onRoomCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismissed()
        }
    }

    LaunchedEffect(state.roomCreated) {
        if (state.roomCreated) {
            snackbarHostState.showSnackbar("Chat room created")
            onRoomCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create chat room") },
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Room title") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = state.memberEmails,
                onValueChange = onMemberEmailsChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                label = { Text("Member emails") },
                placeholder = { Text("student1@myuniversity.edu, student2@myuniversity.edu") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                supportingText = {
                    Text("Separate multiple emails with commas or new lines. Members must already have StudyConnect accounts.")
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onCreateRoom,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Create room")
                }
            }
        }
    }
}
