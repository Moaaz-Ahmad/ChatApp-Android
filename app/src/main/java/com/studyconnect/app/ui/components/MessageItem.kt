package com.studyconnect.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.studyconnect.app.data.model.Message
import java.text.DateFormat
import java.util.Date

@Composable
fun MessageItem(
    message: Message,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isOwnMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            if (!isOwnMessage) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isOwnMessage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = if (isOwnMessage) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT)
    return formatter.format(Date(timestamp))
}
