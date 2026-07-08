package com.example.todowithspirits.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

val spiritsTodoColor = SpiritsTodoColor(
    homeColor = Color(0xFFF6F6F6),
    trackColor = Color(0xFF353534),
    trackColor2 = Color(0xFFEAEAEA),
    surfaceColor1 = Color(0xFFF3E8FF),
    surfaceColor2 = Color(0xFFF6F6F6),
    surfaceColor3 = Color(0xFFD7BFFF),
    surfaceColor4 = Color(0xFFFAFAFA),
    surfaceColor5 = Color(0xFF8DE4FF),
    onSurfaceColor1 = Color(0xFFB286FD),
    onSurfaceColor2 = Color(0xFFBCBCBC),
    onSurfaceColor3 = Color(0xFFD1D1D6),
    onSurfaceColor4 = Color(0xFF8DE4FF),
    onSurfaceColor5 = Color(0xFFB2F042),
    onSurfaceColor6 = Color(0xFFF49191),
    onSurfaceColor7 = Color(0xFFC7C7C7),
    onSurfaceColor8 = Color(0xFFF7F7F7),
    onSurfaceColor9 = Color(0xFFECECEC),
    onSurfaceColor10 = Color(0xFFF6F6F6),
    onSurfaceColor11 = Color(0xFFD9D9D9),
    selectedColor = Color(0xFF374957),
    selectedTabColor = Color(0xFFB286FD),
    unselectedColor = Color(0xFFD1D1D6),
    unselectedTabBgColor = Color(0xFFF6F6F6),
    titleTextColor = Color(0xFF000000),
    hintTextColor = Color(0xFF888888),
    selectedDateTextColor = Color(0xFFB286FD),
    selectedDateBoxColor = Color(0xFFD7BFFF),
    mainTextColor = Color(0xFF857563),
    textColor1 = Color(0xFFC2C2C2),
    textColor2 = Color(0xFFB286FD),
    bgColor1 = Color(0xFFFAFAFA),
    bgColor2 = Color(0xFFFFFFFF),
    dividerColor = Color(0xFFD1D1D6),
    selectedTimeBoxColor = Color(0xFFF4ECFC),
    buttonColor = Color(0xFFD7BFFF),
    white = Color(0xFFFFFFFF),
    transparent = Color(0x00000000)
)

class SpiritsTodoColor(
    homeColor: Color,
    trackColor: Color,
    trackColor2: Color,
    surfaceColor1: Color,
    surfaceColor2: Color,
    surfaceColor3: Color,
    surfaceColor4: Color,
    surfaceColor5: Color,
    onSurfaceColor1: Color,
    onSurfaceColor2: Color,
    onSurfaceColor3: Color,
    onSurfaceColor4: Color,
    onSurfaceColor5: Color,
    onSurfaceColor6: Color,
    onSurfaceColor7: Color,
    onSurfaceColor8: Color,
    onSurfaceColor9: Color,
    onSurfaceColor10: Color,
    onSurfaceColor11: Color,
    selectedColor: Color,
    selectedTabColor: Color,
    unselectedColor: Color,
    unselectedTabBgColor: Color,
    titleTextColor: Color,
    hintTextColor: Color,
    selectedDateTextColor: Color,
    selectedDateBoxColor: Color,
    mainTextColor: Color,
    textColor1: Color,
    textColor2: Color,
    bgColor1: Color,
    bgColor2: Color,
    dividerColor: Color,
    selectedTimeBoxColor: Color,
    buttonColor: Color,
    white: Color,
    transparent: Color
) {
    var homeColor by mutableStateOf(homeColor)
        private set

    var trackColor by mutableStateOf(trackColor)
        private set

    var trackColor2 by mutableStateOf(trackColor2)
        private set

    var surfaceColor1 by mutableStateOf(surfaceColor1)
        private set

    var surfaceColor2 by mutableStateOf(surfaceColor2)
        private set

    var surfaceColor3 by mutableStateOf(surfaceColor3)
        private set

    var surfaceColor4 by mutableStateOf(surfaceColor4)
        private set

    var surfaceColor5 by mutableStateOf(surfaceColor5)
        private set

    var onSurfaceColor1 by mutableStateOf(onSurfaceColor1)
        private set

    var onSurfaceColor2 by mutableStateOf(onSurfaceColor2)
        private set

    var onSurfaceColor3 by mutableStateOf(onSurfaceColor3)
        private set

    var onSurfaceColor4 by mutableStateOf(onSurfaceColor4)
        private set

    var onSurfaceColor5 by mutableStateOf(onSurfaceColor5)
        private set

    var onSurfaceColor6 by mutableStateOf(onSurfaceColor6)
        private set

    var onSurfaceColor7 by mutableStateOf(onSurfaceColor7)
        private set

    var onSurfaceColor8 by mutableStateOf(onSurfaceColor8)
        private set

    var onSurfaceColor9 by mutableStateOf(onSurfaceColor9)
        private set

    var onSurfaceColor10 by mutableStateOf(onSurfaceColor10)
        private set

    var onSurfaceColor11 by mutableStateOf(onSurfaceColor11)
        private set

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

    var selectedDateBoxColor by mutableStateOf(selectedDateBoxColor)
        private set

    var mainTextColor by mutableStateOf(mainTextColor)
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

    var selectedTimeBoxColor by mutableStateOf(selectedTimeBoxColor)
        private set

    var buttonColor by mutableStateOf(buttonColor)
        private set

    var white by mutableStateOf(white)
        private set

    var transparent by mutableStateOf(transparent)
        private set
}