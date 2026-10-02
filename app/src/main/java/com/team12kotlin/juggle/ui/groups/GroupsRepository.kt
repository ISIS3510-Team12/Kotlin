package com.team12kotlin.juggle.ui.groups

import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory stand-in for a groups data source, shared across screens so a group
 * edited in one place (e.g. Edit Group) is reflected everywhere else (e.g. the
 * Groups list and Group Detail). No persistence/network layer exists yet (the
 * backend only exposes read endpoints for groups so far).
 */
object GroupsRepository {

    /** Mock university directory available to add to any group. */
    val directory: List<User> = listOf(
        User(userId = "u1", firstName = "Diego Alejandro", email = "d.munevar@uniandes.edu.co", major = "Comp Sci"),
        User(userId = "u2", firstName = "Manuela", email = "m.loveral@uniandes.edu.co", major = "Comp Sci"),
        User(userId = "u3", firstName = "Shaiel Mateo", email = "sm.jimenez@uniandes.edu.co", major = "Comp Sci"),
        User(userId = "u4", firstName = "Juan Diego", email = "jd.perez@uniandes.edu.co", major = "Comp Sci"),
        User(userId = "u5", firstName = "Snoopy", email = "snoopy@uniandes.edu.co", major = "Comp Sci")
    )

    private val _groups = MutableStateFlow(
        listOf(
            Group(id = 1, name = "The best group", description = "we got this!", users = directory.toMutableList()),
            Group(id = 2, name = "Academic Victims", description = "only one of us is going to survive the semester", users = directory.take(2).toMutableList()),
            Group(id = 3, name = "Mobile divas", description = "Slaaaaaayyyyyy", users = directory.takeLast(3).toMutableList())
        )
    )
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    fun findById(groupId: Int): Group? = _groups.value.find { it.id == groupId }

    fun updateGroup(groupId: Int, transform: (Group) -> Group) {
        _groups.update { current ->
            current.map { group -> if (group.id == groupId) transform(group) else group }
        }
    }
}
