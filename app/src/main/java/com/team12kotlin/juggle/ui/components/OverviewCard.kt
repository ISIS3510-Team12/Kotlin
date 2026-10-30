package com.team12kotlin.juggle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Emoji_food_beverage
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun OverviewCard(
    icon: ImageVector,
    title: String,
    count: Int?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(size = 16.dp))
            .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            contentColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = modifier
                    .widthIn(min = 0.dp)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title
                )
                Text(
                    text = "Today's $title",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = count?.toString() ?: "\u2013",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OverviewCardPreview() {
    JuggleTheme() {
        OverviewCard(icon = MaterialSymbols.Outlined.Emoji_food_beverage, title = "Coffees", count=10)
    }
}

@Preview(showBackground = true)
@Composable
private fun OverviewCardPreviewDark() {
    JuggleTheme(darkTheme = true) {
        OverviewCard(icon = MaterialSymbols.Outlined.Emoji_food_beverage, title = "Coffees", count=10)
    }
}