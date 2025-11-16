package com.studyconnect.app.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.studyconnect.app.data.model.ChatRoom
import com.studyconnect.app.ui.viewmodel.ChatListUiState
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    state: ChatListUiState,
    snackbarHostState: SnackbarHostState,
    onRoomSelected: (ChatRoom) -> Unit,
    onProfileClick: () -> Unit,
    onCreateRoom: () -> Unit,
    onSignOut: () -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismissed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StudyConnect Chats") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile")
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Outlined.Logout, contentDescription = "Sign out")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRoom) {
                Icon(Icons.Rounded.Add, contentDescription = "Create chat room")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.rooms.isEmpty() -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No chat rooms yet. Check back soon!")
                }
            }

            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.rooms, key = { it.id }) { room ->
                        ChatRoomCard(room = room, onClick = { onRoomSelected(room) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRoomCard(room: ChatRoom, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = room.title.ifBlank { "Untitled Room" },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = room.lastMessage.ifBlank { "No messages yet" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatTimestamp(room.updatedAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestamp))
}
