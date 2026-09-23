package com.team12kotlin.juggle.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team12kotlin.juggle.R
import com.team12kotlin.juggle.ui.components.PillButton
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onGetStartedClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // Hero: centered logo on the primary background.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.5f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_dark),
                contentDescription = "Juggle",
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.65f)
                    .padding(horizontal = 24.dp)
            )
        }

        // Bottom sheet with rounded top corners overlapping the hero.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.56f),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                OnboardingProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = 0.5f
                )

                Spacer(modifier = Modifier.height(1.dp))

                // Illustration fills the flexible space between progress and text.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_multitasking),
                        contentDescription = null,
                        modifier = Modifier.size(255.dp)
                    )
                }

                Text(
                    text = "Worried about your tasks?\nManage them all in one place.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                PillButton(
                    text = "Get started",
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


@Composable
private fun OnboardingProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val trackShape = RoundedCornerShape(percent = 50)
    Box(
        modifier = modifier.height(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(trackShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .weight((1f - progress).coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(trackShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    JuggleTheme {
        OnboardingScreen()
    }
}
