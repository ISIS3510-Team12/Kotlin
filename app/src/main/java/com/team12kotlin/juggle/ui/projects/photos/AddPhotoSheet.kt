package com.team12kotlin.juggle.ui.projects.photos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.team12kotlin.juggle.data.local.DevicePhoto
import com.team12kotlin.juggle.ui.components.OfflineActionNotice

// Same limit as the server.
private const val MAX_CAPTION_LENGTH = 200

private val PHOTO_PERMISSIONS: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
    } else {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    }

private fun hasPhotoAccess(context: Context) = PHOTO_PERMISSIONS.any {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

// The phone's photos, with multi selection.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPhotoSheet(
    recent: List<DevicePhoto>,
    isOffline: Boolean,
    onLoadRecent: () -> Unit,
    onUpload: (List<Uri>, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasAccess by remember { mutableStateOf(hasPhotoAccess(context)) }
    var selected by remember { mutableStateOf(emptyList<Uri>()) }
    var caption by remember { mutableStateOf("") }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasAccess = hasPhotoAccess(context)
        onLoadRecent()
    }
    LaunchedEffect(Unit) {
        if (hasAccess) onLoadRecent() else permissionLauncher.launch(PHOTO_PERMISSIONS)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        )
    ) {
        Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "Add photo",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(recent, key = { it.uri.toString() }) { photo ->
                    val isSelected = photo.uri in selected
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .toggleable(value = isSelected) { checked ->
                                selected = if (checked) selected + photo.uri else selected - photo.uri
                            }
                    ) {
                        PhotoImage(
                            source = PhotoSource.Local(photo.uri),
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = "Phone photo"
                        )
                        if (isSelected) {
                            Checkbox(
                                checked = true,
                                onCheckedChange = null,
                                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                            )
                        }
                    }
                }
            }
            if (!hasAccess) {
                TextButton(onClick = { permissionLauncher.launch(PHOTO_PERMISSIONS) }) {
                    Text("Allow photo access to see your gallery")
                }
            }
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it.take(MAX_CAPTION_LENGTH) },
                label = { Text("Caption (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OfflineActionNotice(isOffline = isOffline, modifier = Modifier.padding(top = 8.dp))
            Button(
                onClick = {
                    onUpload(selected, caption.trim().ifEmpty { null })
                    onDismiss()
                },
                enabled = selected.isNotEmpty() && !isOffline,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Text(if (selected.size > 1) "Upload ${selected.size} photos" else "Upload photo")
            }
        }
    }
}
