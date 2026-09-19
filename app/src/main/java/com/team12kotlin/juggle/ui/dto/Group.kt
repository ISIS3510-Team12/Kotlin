package com.team12kotlin.juggle.ui.dto

data class Group(
    val name: String,
    val description: String,
    val members: MutableList<User> = mutableListOf(),
    val id: String = java.util.UUID.randomUUID().toString()
)
