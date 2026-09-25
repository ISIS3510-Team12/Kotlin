package com.team12kotlin.juggle.ui.navbar

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Calendar_month
import com.composables.icons.materialsymbols.outlined.Checklist
import com.composables.icons.materialsymbols.outlined.Home
import com.composables.icons.materialsymbols.outlined.Person
import com.composables.icons.materialsymbols.outlined.Preview_off
import com.composables.icons.materialsymbols.outlinedfilled.Groups

enum class NavigationDestination(
    val route: String,
    val label: String,
    val contentDescription: String,
    val icon: ImageVector,
    val showInNavBar: Boolean = false
) {
    Onboarding(
        route = "onboarding",
        label = "Onboarding",
        contentDescription = "Onboarding",
        icon = MaterialSymbols.Outlined.Home
    ),
    SignUp(
        route = "signup",
        label = "Sign Up",
        contentDescription = "Sign Up",
        icon = MaterialSymbols.Outlined.Person
    ),
    SignIn(
        route = "signin",
        label = "Sign In",
        contentDescription = "Sign In",
        icon = MaterialSymbols.Outlined.Person
    ),
    Home(
        route = "home",
        label = "Home",
        contentDescription = "Home",
        icon = MaterialSymbols.Outlined.Home,
        showInNavBar = true
    ),
    Tasks(
        route = "tasks",
        label = "Tasks",
        contentDescription = "Tasks",
        icon = MaterialSymbols.Outlined.Checklist,
        showInNavBar = true
    ),
    AllTasks(
        route = "tasks/all",
        label = "All Tasks",
        contentDescription = "All Tasks",
        icon = MaterialSymbols.Outlined.Checklist,
    ),
    CreateTask(
        route = "tasks/create",
        label = "Create Task",
        contentDescription = "Create Task",
        icon = MaterialSymbols.Outlined.Checklist,
    ),
    Groups(
        route = "groups",
        label = "Groups",
        contentDescription = "Groups",
        icon = MaterialSymbols.OutlinedFilled.Groups,
        showInNavBar = true
    ),

    CreateGroup(
        route = "groups/create",
        label = "Create Groups",
        contentDescription = "Create Groups",
        icon = MaterialSymbols.OutlinedFilled.Groups,
    ),
    Calendar(
        route = "calendar",
        label = "Calendar",
        contentDescription = "Calendar",
        icon = MaterialSymbols.Outlined.Calendar_month,
        showInNavBar = true
    ),

    Profile(
        route = "profile",
        label = "Profile",
        contentDescription = "Profile",
        icon = MaterialSymbols.Outlined.Preview_off
    );

    companion object {
        fun fromRoute(route: String?): NavigationDestination? =
            entries.firstOrNull { it.route == route }
    }
}
