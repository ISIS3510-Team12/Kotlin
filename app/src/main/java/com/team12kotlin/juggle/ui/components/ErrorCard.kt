package com.team12kotlin.juggle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Error
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(size = 16.dp))
    ) {
        Surface(
            color = MaterialTheme.colorScheme.outlineVariant,
            contentColor = MaterialTheme.colorScheme.error
        ) {
            Column(
                modifier = modifier
                    .widthIn(min = 0.dp)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = MaterialSymbols.Outlined.Error,
                    contentDescription = "Error: $message",
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
                if (onRetry != null) {
                    TextButton(onClick = onRetry) {
                        Text("Try again")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorCardPreview() {
    JuggleTheme {
        ErrorCard(message = "418")
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorCardPreviewDark() {
    JuggleTheme(darkTheme = true) {
        ErrorCard(message = "418")
    }
}