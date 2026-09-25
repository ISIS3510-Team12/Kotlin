package com.team12kotlin.juggle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.team12kotlin.juggle.ui.auth.SignInScreen
import com.team12kotlin.juggle.ui.auth.SignUpScreen
import com.team12kotlin.juggle.ui.groups.GroupsScreen
import com.team12kotlin.juggle.ui.groups.create.CreateGroupScreen
import com.team12kotlin.juggle.ui.navbar.AppNavigationBar
import com.team12kotlin.juggle.ui.navbar.NavbarViewModel
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.onboarding.OnboardingScreen
import com.team12kotlin.juggle.ui.tasks.TasksScreen
import com.team12kotlin.juggle.ui.tasks.allTasks.AllTasksScreen
import com.team12kotlin.juggle.ui.tasks.create.CreateTaskScreen
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
        // Avoid the double top-bar in some devices
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        )
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = NavigationDestination.Onboarding.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavigationDestination.Onboarding.route) {
                    OnboardingScreen(
                        modifier = Modifier.fillMaxSize(),
                        onGetStartedClick = {
                            navController.navigate(NavigationDestination.SignUp.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(NavigationDestination.SignUp.route) {
                    SignUpScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBackClick = { navController.popBackStack() },
                        onSignUpClick = {
                            navController.navigate(NavigationDestination.Tasks.route) {
                                popUpTo(NavigationDestination.Onboarding.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onSignInClick = {
                            navController.navigate(NavigationDestination.SignIn.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(NavigationDestination.SignIn.route) {
                    SignInScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBackClick = { navController.popBackStack() },
                        onSignInClick = {
                            navController.navigate(NavigationDestination.Tasks.route) {
                                popUpTo(NavigationDestination.Onboarding.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onSignUpClick = {
                            navController.navigate(NavigationDestination.SignUp.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(NavigationDestination.Home.route) {
                    PlaceholderScreen(
                        label = "Home",
                        onProfileClick = { navController.navigate(it.route) }
                    )
                }
                composable(NavigationDestination.Tasks.route) {
                    TasksScreen(
                        modifier = Modifier.fillMaxSize(),
                        onProfileClick = { navController.navigate(it.route) },
                        onAllTasksClick = {
                            navController.navigate(NavigationDestination.AllTasks.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToCreateTask = {
                            navController.navigate(NavigationDestination.CreateTask.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(NavigationDestination.AllTasks.route) {
                    AllTasksScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(NavigationDestination.CreateTask.route) {
                    CreateTaskScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBackClick = { navController.popBackStack() },
                        onTaskCreated = { navController.popBackStack() }
                    )
                }
                composable(NavigationDestination.Groups.route) {
                    GroupsScreen(
                        modifier = Modifier.fillMaxSize(),
                        onProfileClick = { navController.navigate(it.route) },
                        onNavigateToCreateGroup = {
                            navController.navigate(NavigationDestination.CreateGroup.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(NavigationDestination.CreateGroup.route) {
                    CreateGroupScreen(
                        modifier = Modifier,
                        goBack = {
                            navController.navigate(NavigationDestination.Groups.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(NavigationDestination.Calendar.route) {
                    PlaceholderScreen(
                        label = "Calendar",
                        onProfileClick = { navController.navigate(it.route) }
                    )
                }
                composable(NavigationDestination.Profile.route) {
                    PlaceholderScreen(
                        label = "Profile",
                        onProfileClick = { navController.navigate(it.route) }
                    )
                }

            }
            // Avoid the nav bar from appearing on the onboarding, sign in and sign up.
            if (currentRoute != NavigationDestination.Onboarding.route &&
                currentRoute != NavigationDestination.SignUp.route &&
                currentRoute != NavigationDestination.SignIn.route
            ) {
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
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    label: String,
    modifier: Modifier = Modifier,
    onProfileClick: (NavigationDestination) -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(onProfileClick = onProfileClick)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Text(label)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppRootPreview() {
    JuggleTheme {
        AppRoot()
    }
}
