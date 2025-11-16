package com.studyconnect.app.ui.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Send
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.studyconnect.app.ui.components.MessageItem
import com.studyconnect.app.ui.viewmodel.ChatRoomUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    state: ChatRoomUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onMessageChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismissed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.roomTitle.ifBlank { "Chat" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile")
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
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        MessageItem(
                            message = message,
                            isOwnMessage = message.senderId == state.currentUserId
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            MessageInputRow(
                message = state.messageInput,
                isSending = state.isSending,
                onMessageChanged = onMessageChanged,
                onSendMessage = onSendMessage
            )
        }
    }
}

@Composable
private fun MessageInputRow(
    message: String,
    isSending: Boolean,
    onMessageChanged: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message StudyConnect...") },
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendMessage() })
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSendMessage, enabled = message.isNotBlank() && !isSending) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Outlined.Send, contentDescription = "Send message")
            }
        }
    }
}
