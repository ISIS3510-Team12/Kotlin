package com.team12kotlin.juggle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.team12kotlin.juggle.ui.navbar.AppNavigationBar
import com.team12kotlin.juggle.ui.tasks.TasksScreen
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.topbar.AppTopBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JuggleTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppTopBar()
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { },
                            icon = {
                                Icon(
                                    MaterialSymbols.Outlined.Add,
                                    contentDescription = "Add"
                                )
                            },
                            text = {
                                Text(
                                    text = "Create Task",
                                    fontSize = 16.sp
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    },
                    bottomBar = {
                        AppNavigationBar()
                    }
                ) { innerPadding ->
                    TasksScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}
