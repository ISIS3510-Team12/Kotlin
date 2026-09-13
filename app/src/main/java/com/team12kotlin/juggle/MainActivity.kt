package com.team12kotlin.juggle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.team12kotlin.juggle.ui.theme.JuggleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JuggleTheme {
                AppRoot()
            }
        }
    }
}
