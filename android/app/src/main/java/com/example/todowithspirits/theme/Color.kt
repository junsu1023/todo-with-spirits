package com.example.todowithspirits.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

val splitsTodoColor = SplitsTodoColor(
    selectedColor = Color(0xFF374957),
    selectedTabColor = Color(0xFFB286FD),
    unselectedColor = Color(0xFFD1D1D6),
    unselectedTabBgColor = Color(0xFFF6F6F6),
    titleTextColor = Color(0xFF000000),
    hintTextColor = Color(0xFF888888),
    selectedDateTextColor = Color(0xFFB286FD),
    mainTextColor = Color(0xFF857563),
    textColor1 = Color(0xFFC2C2C2),
    bgColor1 = Color(0xFFFAFAFA),
    bgColor2 = Color(0xFFFFFFFF),
    dividerColor = Color(0xFFD1D1D6),
    white = Color(0xFFFFFFFF),
    transparent = Color(0x00000000)
)

class SplitsTodoColor(
    selectedColor: Color,
    selectedTabColor: Color,
    unselectedColor: Color,
    unselectedTabBgColor: Color,
    titleTextColor: Color,
    hintTextColor: Color,
    selectedDateTextColor: Color,
    mainTextColor: Color,
    textColor1: Color,
    bgColor1: Color,
    bgColor2: Color,
    dividerColor: Color,
    white: Color,
    transparent: Color
) {
    var selectedColor by mutableStateOf(selectedColor)
        private set

    var selectedTabColor by mutableStateOf(selectedTabColor)
        private set

    var unselectedColor by mutableStateOf(unselectedColor)
        private set

    var unselectedTabBgColor by mutableStateOf(unselectedTabBgColor)
        private set

    var titleTextColor by mutableStateOf(titleTextColor)
        private set

    var hintTextColor by mutableStateOf(hintTextColor)
        private set

    var selectedDateTextColor by mutableStateOf(selectedDateTextColor)
        private set

    var mainTextColor by mutableStateOf(mainTextColor)
        private set

    var textColor1 by mutableStateOf(textColor1)
        private set

    var bgColor1 by mutableStateOf(bgColor1)
        private set

    var bgColor2 by mutableStateOf(bgColor2)
        private set

    var dividerColor by mutableStateOf(dividerColor)
        private set

    var white by mutableStateOf(white)
        private set

    var transparent by mutableStateOf(transparent)
        private set
}