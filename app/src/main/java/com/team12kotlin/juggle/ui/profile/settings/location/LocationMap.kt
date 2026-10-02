package com.team12kotlin.juggle.ui.profile.settings.location

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.team12kotlin.juggle.ui.dto.MAX_NOTIFY_WITHIN_METERS

// Default map center.
private val DEFAULT_CENTER = LatLng(4.6014, -74.0661)
private const val DEFAULT_ZOOM = 12f
private const val PICKED_ZOOM = 16f

/** Map used to pick the reminder location. */
@Composable
fun LocationMap(
    latitude: Double?,
    longitude: Double?,
    radiusMeters: Int?,
    onLocationPicked: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val picked = if (latitude != null && longitude != null) LatLng(latitude, longitude) else null
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(picked ?: DEFAULT_CENTER, if (picked != null) PICKED_ZOOM else DEFAULT_ZOOM)
    }

    // Move the camera to the selected location.
    LaunchedEffect(picked) {
        picked?.let { cameraPositionState.animate(CameraUpdateFactory.newLatLng(it)) }
    }

    GoogleMap(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp)),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
        onMapClick = { onLocationPicked(it.latitude, it.longitude) }
    ) {
        if (picked != null) {
            Marker(state = remember(picked) { MarkerState(position = picked) })
            Circle(
                center = picked,
                // Radius limited to the allowed range.
                radius = (radiusMeters ?: 0).coerceIn(1, MAX_NOTIFY_WITHIN_METERS).toDouble(),
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                strokeColor = MaterialTheme.colorScheme.primary,
                strokeWidth = 3f
            )
        }
    }
}
