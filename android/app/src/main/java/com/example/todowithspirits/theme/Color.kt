package com.example.todowithspirits.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

val splitsTodoColor = SplitsTodoColor(
    selectedColor = Color(0xFF374957),
    unselectedColor = Color(0xFFD1D1D6),
    titleTextColor = Color(0xFF000000),
    textColor1 = Color(0xFF857563),
    textColor2 = Color(0xFF8F8170),
    bgColor1 = Color(0xFFFAFAFA),
    bgColor2 = Color(0xFFFFFFFF),
    dividerColor = Color(0xFFD1D1D6),
    transparent = Color(0x00000000)
)

class SplitsTodoColor(
    selectedColor: Color,
    unselectedColor: Color,
    titleTextColor: Color,
    textColor1: Color,
    textColor2: Color,
    bgColor1: Color,
    bgColor2: Color,
    dividerColor: Color,
    transparent: Color
) {
    var selectedColor by mutableStateOf(selectedColor)
        private set

    var unselectedColor by mutableStateOf(unselectedColor)
        private set

    var titleTextColor by mutableStateOf(titleTextColor)
        private set

    var textColor1 by mutableStateOf(textColor1)
        private set

    var textColor2 by mutableStateOf(textColor2)
        private set

    var bgColor1 by mutableStateOf(bgColor1)
        private set

    var bgColor2 by mutableStateOf(bgColor2)
        private set

    var dividerColor by mutableStateOf(dividerColor)
        private set

    var transparent by mutableStateOf(transparent)
        private set
}