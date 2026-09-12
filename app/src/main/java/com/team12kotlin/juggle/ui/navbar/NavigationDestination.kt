package com.team12kotlin.juggle.ui.navbar

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Calendar_month
import com.composables.icons.materialsymbols.outlined.Checklist
import com.composables.icons.materialsymbols.outlined.Home
import com.composables.icons.materialsymbols.outlinedfilled.Groups

enum class NavigationDestination(
    val label: String,
    val contentDescription: String,
    val icon: ImageVector
) {
    Home(
        label = "Home",
        contentDescription = "Home",
        icon = MaterialSymbols.Outlined.Home
    ),
    Tasks(
        label = "Tasks",
        contentDescription = "Tasks",
        icon = MaterialSymbols.Outlined.Checklist
    ),
    Groups(
        label = "Groups",
        contentDescription = "Groups",
        icon = MaterialSymbols.OutlinedFilled.Groups
    ),
    Calendar(
        label = "Calendar",
        contentDescription = "Calendar",
        icon = MaterialSymbols.Outlined.Calendar_month
    );
}
