package com.example.todowithspirits.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

val splitsTodoColor = SplitsTodoColor(
    textColor1 = Color(0xFFB286FD),
    textColor2 = Color(0xFF8F8170),
    bgColor1 = Color(0xFFFAFAFA)
)

class SplitsTodoColor(
    textColor1: Color,
    textColor2: Color,
    bgColor1: Color
) {
    var textColor1 by mutableStateOf(textColor1)
        private set

    var textColor2 by mutableStateOf(textColor2)
        private set

    var bgColor1 by mutableStateOf(bgColor1)
        private set
}