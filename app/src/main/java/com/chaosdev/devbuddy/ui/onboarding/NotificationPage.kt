package com.chaosdev.devbuddy.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NotificationPage(
    notificationEnabled: Boolean,
    onNotificationToggled: (Boolean) -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "Enable daily reading prompt",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Get a daily prompt to read an A1-curated article",
                        style = MaterialTheme.typography.bodySmall,

                        )
                }
            }
            Switch(
                checked = notificationEnabled,
                onCheckedChange = onNotificationToggled
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onFinish,

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finish")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationPagePreview() {
    NotificationPage(
        notificationEnabled = true,
        onNotificationToggled = {},
        onFinish = {}
    )
}
