package com.team12kotlin.juggle.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_right
import com.composables.icons.materialsymbols.outlined.Arrow_right_alt
import com.composables.icons.materialsymbols.outlined.Group
import com.team12kotlin.juggle.ui.components.IconMonogram
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun GroupCard(
    group: Group,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    groupIcon: ImageVector = MaterialSymbols.Outlined.Group,
    arrowIcon: ImageVector = MaterialSymbols.Outlined.Arrow_right_alt
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconMonogram(
                icon = groupIcon,
                contentDescription = "Group",
                containerColor = containerColor,
                contentColor = contentColor
            )

            Text(
                text = group.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = arrowIcon,
                contentDescription = "Open group"
            )
        }
    }
}

@Preview
@Composable
private fun GroupCardPreview() {
    JuggleTheme {
        GroupCard(Group(name = "The best group", description = "un grupo"))
    }
}