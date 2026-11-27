package com.team12kotlin.juggle.ui.projects.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add_a_photo
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.OfflineActionNotice
import com.team12kotlin.juggle.ui.connectivity.RefreshOnReconnect
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import com.team12kotlin.juggle.utils.epochSecondsToLocalDay
import com.team12kotlin.juggle.utils.longLabel
import com.team12kotlin.juggle.utils.utcToLocalDay
import kotlinx.coroutines.delay

private const val NOTICE_MS = 3000L

@Composable
fun ProjectGalleryScreen(
    modifier: Modifier = Modifier,
    viewModel: ProjectPhotosViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.ProjectGallery, uiState.isLoading)
    val isOnline = rememberIsOnline()
    val isOffline = isOnline == false
    RefreshOnReconnect(isOnline = isOnline, onReconnect = viewModel::load)
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(uiState.notice) {
        if (uiState.notice != null) {
            delay(NOTICE_MS)
            viewModel.onNoticeShown()
        }
    }

    var addOpen by rememberSaveable { mutableStateOf(false) }
    var viewerIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    val viewerPhotos = when (uiState.filter) {
        GalleryFilter.ALL -> uiState.photos.map { photo ->
            val date = utcToLocalDay(photo.createdAt)?.longLabel().orEmpty()
            ViewerPhoto(
                source = PhotoSource.Remote(photo),
                title = photo.caption?.takeIf { it.isNotBlank() } ?: "Project photo",
                subtitle = "Added by ${photo.uploaderName} - $date",
                canDelete = photo.uploaderId == uiState.currentUserId
            )
        }
        GalleryFilter.SAVED -> uiState.saved.map { photo ->
            ViewerPhoto(
                PhotoSource.Local(photo.uri),
                "Saved photo",
                "Saved on ${epochSecondsToLocalDay(photo.addedAtSeconds).longLabel()}"
            )
        }
    }
    // Offline, only the saved photos can be viewed. Waits for the viewer to close,
    // so the photos behind it don't change while it is open.
    LaunchedEffect(isOffline, viewerIndex == null) {
        if (isOffline && viewerIndex == null) viewModel.onFilterSelected(GalleryFilter.SAVED)
    }

    val showAll = uiState.filter == GalleryFilter.ALL
    val isEmpty = !uiState.isLoading && uiState.errorMessage == null &&
        uiState.photos.isEmpty() && uiState.uploads.isEmpty()

    Scaffold(
        modifier = modifier,
        topBar = { BackTopBar(title = uiState.projectName.ifBlank { "Photos" }, onBack = onBackClick) },
        floatingActionButton = {
            val fabColors = MaterialTheme.colorScheme
            ExtendedFloatingActionButton(
                onClick = { if (!isOffline) addOpen = true },
                containerColor = if (isOffline) fabColors.surfaceContainerHighest else fabColors.primaryContainer,
                contentColor = if (isOffline) fabColors.onSurface.copy(alpha = 0.38f)
                else fabColors.onPrimaryContainer,
                icon = { Icon(MaterialSymbols.Outlined.Add_a_photo, contentDescription = null) },
                text = { Text("Add photo") },
                modifier = Modifier
                    .padding(bottom = 96.dp)
                    .semantics { if (isOffline) disabled() }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = uiState.filter.ordinal) {
                GalleryFilter.entries.forEach { filter ->
                    Tab(
                        selected = uiState.filter == filter,
                        onClick = { viewModel.onFilterSelected(filter) },
                        text = { Text(filter.label) }
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 180.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isOffline) fullWidth { OfflineActionNotice(isOffline = true) }
                uiState.notice?.let { fullWidth { Text(it, style = MaterialTheme.typography.bodyMedium) } }

                if (showAll && isOffline) {
                    fullWidth { EmptyMessage("You're offline. Only saved photos can be viewed.") }
                    uploadTiles(uiState.uploads, isOffline, viewModel::retry)
                } else if (showAll) {
                    uiState.errorMessage?.let { message ->
                        fullWidth { ErrorCard(message = message, onRetry = viewModel::load) }
                    }
                    if (uiState.isLoading && uiState.photos.isEmpty()) {
                        fullWidth {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    if (isEmpty) fullWidth { EmptyMessage("No photos yet") }
                    uploadTiles(uiState.uploads, isOffline, viewModel::retry)
                    items(uiState.photos.size, key = { uiState.photos[it].id }) { index ->
                        PhotoTile(PhotoSource.Remote(uiState.photos[index]), onClick = { viewerIndex = index })
                    }
                } else {
                    if (uiState.saved.isEmpty()) {
                        fullWidth { EmptyMessage("Photos you save to this phone appear here.") }
                    }
                    items(uiState.saved.size, key = { uiState.saved[it].uri.toString() }) { index ->
                        PhotoTile(PhotoSource.Local(uiState.saved[index].uri), onClick = { viewerIndex = index })
                    }
                }
            }
        }
    }

    if (addOpen) {
        AddPhotoSheet(
            recent = uiState.recent,
            isOffline = isOffline,
            onLoadRecent = viewModel::loadRecent,
            onUpload = viewModel::upload,
            onDismiss = { addOpen = false }
        )
    }

    viewerIndex?.let { index ->
        if (viewerPhotos.isNotEmpty()) {
            PhotoViewer(
                photos = viewerPhotos,
                initialIndex = index.coerceIn(0, viewerPhotos.lastIndex),
                projectName = uiState.projectName,
                notice = uiState.notice,
                isOffline = isOffline,
                onSave = viewModel::save,
                onDelete = viewModel::delete,
                onDismiss = { viewerIndex = null }
            )
        } else if (!uiState.isLoading) {
            LaunchedEffect(Unit) { viewerIndex = null }
        }
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

private fun LazyGridScope.uploadTiles(
    uploads: List<PhotoUpload>,
    isOffline: Boolean,
    onRetry: (PhotoUpload) -> Unit
) {
    items(uploads, key = { "upload:${it.clientUploadId}" }) { upload ->
        UploadTile(upload, isOffline = isOffline, onRetry = { onRetry(upload) })
    }
}

@Composable
private fun PhotoTile(source: PhotoSource, onClick: () -> Unit) {
    PhotoImage(
        source = source,
        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick),
        contentDescription = "Photo"
    )
}

@Composable
private fun UploadTile(upload: PhotoUpload, isOffline: Boolean, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = upload.failed && !isOffline, onClick = onRetry),
        contentAlignment = Alignment.Center
    ) {
        PhotoImage(source = PhotoSource.Local(upload.uri), modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            if (upload.failed) {
                Text(
                    text = if (isOffline) "No connection" else "Tap to retry",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            } else {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.outline,
        textAlign = TextAlign.Center
    )
}
