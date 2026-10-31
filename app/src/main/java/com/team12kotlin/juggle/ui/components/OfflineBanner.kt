package com.team12kotlin.juggle.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Cloud_off
import com.team12kotlin.juggle.data.local.LastSync
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val SYNC_TIME = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun OfflineBanner(
    isOnline: Boolean?,
    modifier: Modifier = Modifier
) {
    if (isOnline != false) return
    val lastSync by LastSync.timestamp.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = MaterialSymbols.Outlined.Cloud_off,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = buildString {
                    append("No internet connection")
                    lastSync?.let { at ->
                        val time = Instant.ofEpochMilli(at)
                            .atZone(ZoneId.systemDefault())
                            .format(SYNC_TIME)
                        append(" - Last synced at ")
                        append(time)
                    }
                },
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
