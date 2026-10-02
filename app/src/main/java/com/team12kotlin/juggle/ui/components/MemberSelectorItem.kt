package com.team12kotlin.juggle.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun MemberSelectorItem (
    member: User,
    modifier: Modifier = Modifier,
    checkedState: Boolean = false,
    containerColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer,
    onChecked: (User) -> Unit = {}
){
    ListItem(
        headlineContent = { Text(member.displayName) },
        supportingContent = { Text(member.email) },
        leadingContent = {
            TextMonogram(
                text = member.displayName.take(1).uppercase(),
                containerColor = containerColor,
                contentColor = contentColor
            )
        },
        trailingContent = {
            Checkbox(
                checked = checkedState,
                onCheckedChange = { onChecked(member) },
                enabled = true
            )
        },
        modifier = modifier.padding(4.dp)
    )
}

@Preview
@Composable
private fun MemberSelectorItemPreview() {
    JuggleTheme() {
        MemberSelectorItem(User(userId = "u1", firstName = "Isabel Ripoll", email = "isa.r@gmail.com", major = "Comp Sci"))
    }
}
