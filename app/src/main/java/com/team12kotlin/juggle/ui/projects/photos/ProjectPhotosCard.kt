package com.team12kotlin.juggle.ui.projects.photos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Chevron_right
import com.team12kotlin.juggle.ui.dto.ProjectPhoto
import com.team12kotlin.juggle.utils.shortLabel
import com.team12kotlin.juggle.utils.utcToLocalDay

// Entry to the gallery from the project detail. photos is newest first, or null if unknown.
@Composable
fun ProjectPhotosCard(
    photos: List<ProjectPhoto>?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Project photos", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = photosSummary(photos),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Icon(MaterialSymbols.Outlined.Chevron_right, contentDescription = "Open gallery")
        }
    }
}

private fun photosSummary(photos: List<ProjectPhoto>?): String = when {
    photos == null -> "Open the gallery"
    photos.isEmpty() -> "No photos yet"
    else -> {
        val count = if (photos.size == 1) "1 photo" else "${photos.size} photos"
        val last = utcToLocalDay(photos.first().createdAt)?.shortLabel()
        if (last == null) count else "$count - Last added $last"
    }
}
