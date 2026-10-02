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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.team12kotlin.juggle.ui.auth.SignInScreen
import com.team12kotlin.juggle.ui.auth.SignUpScreen
import com.team12kotlin.juggle.ui.groups.GroupsScreen
import com.team12kotlin.juggle.ui.groups.create.CreateGroupScreen
import com.team12kotlin.juggle.ui.home.HomeScreen
import com.team12kotlin.juggle.ui.navbar.AppNavigationBar
import com.team12kotlin.juggle.ui.navbar.NavbarViewModel
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.onboarding.OnboardingScreen
import com.team12kotlin.juggle.ui.profile.ProfileScreen
import com.team12kotlin.juggle.ui.profile.information.ProfileInformationScreen
import com.team12kotlin.juggle.ui.profile.notifications.NotificationsScreen
import com.team12kotlin.juggle.ui.profile.settings.SettingsScreen
import com.team12kotlin.juggle.ui.profile.settings.location.LocationRemindersScreen
import com.team12kotlin.juggle.ui.tasks.TasksScreen
import com.team12kotlin.juggle.ui.tasks.allTasks.AllTasksScreen
import com.team12kotlin.juggle.ui.tasks.create.CreateTaskScreen
import com.team12kotlin.juggle.ui.tasks.edit.EditTaskScreen
import com.team12kotlin.juggle.ui.tasks.view.ViewTaskScreen
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.topbar.AppTopBar

private val PROFILE_ROUTES = setOf(
    NavigationDestination.Profile.route,
    NavigationDestination.ProfileInformation.route,
    NavigationDestination.Notifications.route,
    NavigationDestination.Settings.route,
    NavigationDestination.LocationReminders.route
)

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
                    HomeScreen(
                        modifier = Modifier,
                        onProfileClick = { navController.navigate(it.route) },
                        onCreateGroupClick = {
                            navController.navigate(NavigationDestination.CreateGroup.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        // TODO: añadir las otras acciones para create task y ver el detail de una task
                    )
                }
                composable(NavigationDestination.Tasks.route) {
                    TasksScreen(
                        modifier = Modifier.fillMaxSize(),
                        onProfileClick = { navController.navigate(it.route) },
                        onTaskClick = { task ->
                            navController.navigate("${NavigationDestination.ViewTask.route}/${task.id}") {
                                launchSingleTop = true
                            }
                        },
                        onAllTasksClick = {
                            navController.navigate(NavigationDestination.AllTasks.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToCreateTask = { groupId ->
                            navController.navigate("${NavigationDestination.CreateTask.route}?groupId=$groupId") {
                                launchSingleTop = true
                            }
                        },
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
                composable(NavigationDestination.AllTasks.route) {
                    AllTasksScreen(
                        modifier = Modifier.fillMaxSize(),
                        onTaskClick = { task ->
                            navController.navigate("${NavigationDestination.ViewTask.route}/${task.id}") {
                                launchSingleTop = true
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "${NavigationDestination.CreateTask.route}?groupId={groupId}",
                    arguments = listOf(
                        navArgument("groupId") {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) {
                    CreateTaskScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBackClick = { navController.popBackStack() },
                        onTaskCreated = { taskId ->
                            navController.navigate("${NavigationDestination.ViewTask.route}/$taskId") {
                                popUpTo("${NavigationDestination.CreateTask.route}?groupId={groupId}") {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = "${NavigationDestination.ViewTask.route}/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) {
                    ViewTaskScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBackClick = { navController.popBackStack() },
                        onEditTask = { taskId ->
                            navController.navigate("${NavigationDestination.EditTask.route}/$taskId") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = "${NavigationDestination.EditTask.route}/{taskId}?groupId={groupId}",
                    arguments = listOf(
                        navArgument("taskId") { type = NavType.StringType },
                        navArgument("groupId") {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) {
                    EditTaskScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBackClick = { navController.popBackStack() },
                        onTaskEdited = { navController.popBackStack() },
                        onCancel = { navController.popBackStack() }
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
                    ProfileScreen(
                        modifier = Modifier.fillMaxSize(),
                        goBack = { navController.popBackStack() },
                        onProfileInformationClick = {
                            navController.navigate(NavigationDestination.ProfileInformation.route) {
                                launchSingleTop = true
                            }
                        },
                        onNotificationsClick = {
                            navController.navigate(NavigationDestination.Notifications.route) {
                                launchSingleTop = true
                            }
                        },
                        onSettingsClick = {
                            navController.navigate(NavigationDestination.Settings.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(NavigationDestination.ProfileInformation.route) {
                    ProfileInformationScreen(
                        modifier = Modifier.fillMaxSize(),
                        goBack = { navController.popBackStack() }
                    )
                }

                composable(NavigationDestination.Notifications.route) {
                    NotificationsScreen(
                        modifier = Modifier.fillMaxSize(),
                        goBack = { navController.popBackStack() }
                    )
                }

                composable(NavigationDestination.Settings.route) {
                    SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        goBack = { navController.popBackStack() },
                        onLocationRemindersClick = {
                            navController.navigate(NavigationDestination.LocationReminders.route) {
                                launchSingleTop = true
                            }
                        },
                        onSignedOut = {
                            navController.navigate(NavigationDestination.Onboarding.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(NavigationDestination.LocationReminders.route) {
                    LocationRemindersScreen(
                        modifier = Modifier.fillMaxSize(),
                        goBack = { navController.popBackStack() }
                    )
                }

            }
            // Avoid the nav bar from appearing on the onboarding, sign in, sign up and profile screens.
            if (currentRoute != NavigationDestination.Onboarding.route &&
                currentRoute != NavigationDestination.SignUp.route &&
                currentRoute != NavigationDestination.SignIn.route &&
                currentRoute !in PROFILE_ROUTES
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
