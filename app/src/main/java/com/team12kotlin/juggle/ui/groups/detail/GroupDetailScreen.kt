package com.team12kotlin.juggle.ui.groups.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.Check
import com.composables.icons.materialsymbols.outlined.Exclamation
import com.composables.icons.materialsymbols.outlined.Settings
import com.composables.icons.materialsymbols.outlined.Warning
import com.team12kotlin.juggle.ui.components.IconMonogram
import com.team12kotlin.juggle.ui.components.TextMonogram
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    modifier: Modifier = Modifier,
    viewModel: GroupDetailViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    LaunchedEffect(groupId) { viewModel.load(groupId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GroupDetailContent(
        modifier = modifier,
        group = uiState.group,
        relatedProjects = uiState.relatedProjects,
        onBackClick = onBackClick,
        onCreateProjectClick = { viewModel.onCreateProject() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailContent(
    group: Group?,
    relatedProjects: List<Task>,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onCreateProjectClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Group detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = MaterialSymbols.Outlined.Arrow_back,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateProjectClick,
                modifier = Modifier.padding(bottom = 104.dp),
                icon = {
                    Icon(
                        MaterialSymbols.Outlined.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                text = {
                    Text(
                        text = "Create project",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = group?.name ?: "Group not found",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = group?.description.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Members", style = MaterialTheme.typography.headlineMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    group?.members.orEmpty().forEach { member ->
                        GroupMember(member)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Related projects", style = MaterialTheme.typography.headlineMedium)
                relatedProjects.forEach { project ->
                    RelatedProjectCard(project = project)
                }
            }

            Spacer(modifier = Modifier.height(136.dp))
        }
    }
}

@Composable
private fun GroupMember(
    member: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextMonogram(text = member.firstName.take(1).uppercase())
        Text(
            text = member.firstName,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RelatedProjectCard(
    project: Task,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    importantIcon: ImageVector = MaterialSymbols.Outlined.Exclamation,
    normalIcon: ImageVector = MaterialSymbols.Outlined.Check
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (project.member != null) {
                TextMonogram(text = project.member.take(1).uppercase())
            } else {
                IconMonogram(
                    icon = if (project.isImportant) importantIcon else normalIcon,
                    contentDescription = "Project"
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(project.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = project.dueLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (project.isImportant) {
                        Icon(
                            imageVector = MaterialSymbols.Outlined.Warning,
                            contentDescription = "Important",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Settings,
                        contentDescription = "Project options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenPreview() {
    JuggleTheme {
        GroupDetailContent(
            group = Group(
                id = "preview",
                name = "Group name #1",
                description = "ISIS-3510",
                members = mutableListOf(
                    User(firstName = "Diego", email = "d.munevar@uniandes.edu.co", major = "Comp Sci"),
                    User(firstName = "Manuela", email = "m.loveral@uniandes.edu.co", major = "Comp Sci"),
                    User(firstName = "Shaiel", email = "sm.jimenez@uniandes.edu.co", major = "Comp Sci")
                )
            ),
            relatedProjects = listOf(
                Task(id = "proj1", title = "Marketplace prototype", isImportant = true, dueLabel = "Due in 3 days"),
                Task(id = "proj2", title = "Interview synthesis", member = "Manuela", dueLabel = "Due next week")
            )
        )
    }
}
