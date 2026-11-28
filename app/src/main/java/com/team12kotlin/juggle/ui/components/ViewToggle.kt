package com.team12kotlin.juggle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team12kotlin.juggle.ui.theme.JuggleTheme

/** Pill selector to switch between the views of a screen, like Week and Month. */
@Composable
fun <T> ViewToggle(
    entries: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(3.dp)
    ) {
        entries.forEach { entry ->
            val isSelected = entry == selected
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(entry) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label(entry),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ViewTogglePreview() {
    JuggleTheme {
        ViewToggle(
            entries = listOf("List", "Board"),
            selected = "Board",
            label = { it },
            onSelect = {}
        )
    }
}
