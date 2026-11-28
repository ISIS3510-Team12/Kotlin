package com.team12kotlin.juggle.ui.dto

const val MIN_WHEEL_OPTIONS = 2
const val MAX_WHEEL_OPTIONS = 12

data class Wheel(
    val id: Int,
    val name: String,
    val optionCount: Int = 0
)

data class WheelOption(
    val id: Int,
    val text: String
)
