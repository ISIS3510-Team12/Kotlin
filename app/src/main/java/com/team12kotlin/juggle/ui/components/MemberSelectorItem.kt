package com.team12kotlin.juggle.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlin.text.take

@Composable
fun MemberSelectorItem (
    member: User,
    modifier: Modifier = Modifier,
    checkedState: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onChecked: (User) -> Unit = {}
){
    var checked by remember { mutableStateOf(checkedState) }
    ListItem(
        headlineContent = { Text(member.firstName) },
        supportingContent = { Text(member.email) },
        leadingContent = {
            TextMonogram(
                text = member.firstName.take(1).uppercase(),
                containerColor = containerColor,
                contentColor = contentColor
            )
        },
        trailingContent = {
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    checked = !checked
                    onChecked(member)
                },
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
        MemberSelectorItem(User(firstName = "Isabel Ripoll", email="isa.r@gmail.com", major = "Comp Sci"))
    }
}