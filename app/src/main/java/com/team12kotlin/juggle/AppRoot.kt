package com.team12kotlin.juggle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.team12kotlin.juggle.ui.navbar.AppNavigationBar
import com.team12kotlin.juggle.ui.navbar.NavbarViewModel
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.tasks.TasksScreen
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.topbar.AppTopBar

@Composable
fun AppRoot(
    modifier: Modifier = Modifier,
    navbarViewModel: NavbarViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Make the bottom nav bar in sync with the screen in case of system back
    LaunchedEffect(currentRoute) {
        NavigationDestination.fromRoute(currentRoute)?.let {
            navbarViewModel.onDestinationSelected(it)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(onProfileClick = { destination ->
                navController.navigate(destination.route)
            })
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = NavigationDestination.Tasks.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavigationDestination.Home.route) {
                    PlaceholderScreen(label = "Home")
                }
                composable(NavigationDestination.Tasks.route) {
                    TasksScreen(modifier = Modifier.fillMaxSize())
                }
                composable(NavigationDestination.Groups.route) {
                    PlaceholderScreen(label = "Groups")
                }
                composable(NavigationDestination.Calendar.route) {
                    PlaceholderScreen(label = "Calendar")
                }
                composable (NavigationDestination.Profile.route){
                    PlaceholderScreen(label = "Profile")
                }

            }
            AppNavigationBar(
                viewModel = navbarViewModel,
                onDestinationSelected = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            ExtendedFloatingActionButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 128.dp),
                icon = {
                    Icon(
                        MaterialSymbols.Outlined.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                text = {
                    Text(
                        text = "Create Task",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
        Text(label)
}

@Preview(showBackground = true)
@Composable
private fun AppRootPreview() {
    JuggleTheme {
        AppRoot()
    }
}
