package com.team12kotlin.juggle.ui.projects.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Close
import com.team12kotlin.juggle.ui.components.OfflineActionNotice
import com.team12kotlin.juggle.ui.dto.ProjectPhoto

private val VIEWER_BACKGROUND = Color(0xFF131314)

// A photo as the viewer shows it.
data class ViewerPhoto(
    val source: PhotoSource,
    val title: String,
    val subtitle: String,
    val canDelete: Boolean = false
)

// Full screen, swipeable view. Photos from the phone have no Save or Delete,
// and Delete only shows on the photos the user uploaded.
@Composable
fun PhotoViewer(
    photos: List<ViewerPhoto>,
    initialIndex: Int,
    projectName: String,
    notice: String?,
    isOffline: Boolean,
    onSave: (ProjectPhoto) -> Unit,
    onDelete: (ProjectPhoto) -> Unit,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }
    var toDelete by remember { mutableStateOf<ProjectPhoto?>(null) }
    val current = photos.getOrNull(pagerState.currentPage.coerceAtMost(photos.lastIndex)) ?: return
    val remote = (current.source as? PhotoSource.Remote)?.photo
    val buttonColors = IconButtonDefaults.filledIconButtonColors(
        containerColor = Color.White.copy(alpha = 0.12f),
        contentColor = Color.White
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(modifier = Modifier.fillMaxSize().background(VIEWER_BACKGROUND).safeDrawingPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledIconButton(onClick = onDismiss, colors = buttonColors) {
                    Icon(MaterialSymbols.Outlined.Close, contentDescription = "Close")
                }
                Text(
                    text = "${pagerState.currentPage.coerceAtMost(photos.lastIndex) + 1} of ${photos.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.size(40.dp))
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                PhotoImage(
                    source = photos[page].source,
                    modifier = Modifier.fillMaxSize(),
                    maxPx = FULL_PX,
                    contentScale = ContentScale.Fit,
                    contentDescription = photos[page].title
                )
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(current.title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(current.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
                Text(projectName, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                notice?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (remote != null) {
                    OfflineActionNotice(isOffline = isOffline, modifier = Modifier.padding(top = 8.dp))
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onSave(remote) },
                            enabled = !isOffline,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color.White.copy(alpha = 0.12f),
                                disabledContentColor = Color.White.copy(alpha = 0.38f)
                            ),
                        ) { Text("Save to device") }
                        if (current.canDelete) {
                            OutlinedButton(
                                onClick = { toDelete = remote },
                                enabled = !isOffline,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White,
                                    disabledContentColor = Color.White.copy(alpha = 0.38f)
                                )
                            ) { Text("Delete") }
                        }
                    }
                }
            }
        }

        toDelete?.let { photo ->
            AlertDialog(
                onDismissRequest = { toDelete = null },
                title = { Text("Delete photo?") },
                text = { Text("It will be removed for everyone in the project.") },
                confirmButton = {
                    TextButton(enabled = !isOffline, onClick = {
                        toDelete = null
                        onDelete(photo)
                    }) { Text("Delete") }
                },
                dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
            )
        }
    }
}
