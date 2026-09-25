package com.team12kotlin.juggle.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_right_alt
import com.composables.icons.materialsymbols.roundedfilled.Groups
import com.team12kotlin.juggle.ui.home.QuickActionItem
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun QuickActionItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: (NavigationDestination) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick= { onClick }
        ) {
            Row(
                modifier = modifier
                    .padding(start = 16.dp, top = 5.dp, end = 16.dp, bottom = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.surfaceTint,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = MaterialSymbols.Outlined.Arrow_right_alt,
                    contentDescription = title
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickActionItemPreview() {
    JuggleTheme() {
        QuickActionItem(
            title = "Create new group",
            description = "Create a group workspace",
            icon = MaterialSymbols.RoundedFilled.Groups,
        )
    }
}
