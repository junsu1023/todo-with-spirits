package com.example.todowithspirits.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

val spiritsTodoColor = SpiritsTodoColor(
    surfaceColor3 = Color(0xFFD7BFFF),
    surfaceColor4 = Color(0xFFFAFAFA),
    onSurfaceColor1 = Color(0xFFB286FD),
    onSurfaceColor3 = Color(0xFFD1D1D6),
    onSurfaceColor4 = Color(0xFF8DE4FF),
    onSurfaceColor5 = Color(0xFFB2F042),
    onSurfaceColor7 = Color(0xFFC7C7C7),
    onSurfaceColor8 = Color(0xFFF7F7F7),
    onSurfaceColor9 = Color(0xFFECECEC),
    onSurfaceColor11 = Color(0xFFD9D9D9),
    onSurfaceColor12 = Color(0xFF6A66A1),
    mainTextColor = Color(0xFF857563),
    white = Color(0xFFFFFFFF)
)

val spiritsLightColor = SpiritColor(
    surfaceColor1 = Color(0xFFFFFFFF),
    surfaceColor2 = Color(0xFFB286FD),
    surfaceColor3 = Color(0xFFD7BFFF),
    surfaceColor4 = Color(0xFFF6F6F6),
    surfaceColor5 = Color(0xFF353534),
    surfaceColor6 = Color(0xB3F3E8FF),
    surfaceColor7 = Color(0xFFEAEAEA),
    surfaceColor8 = Color(0xFF8DE4FF),
    surfaceColor9 = Color(0xFFB2F042),
    surfaceColor10 = Color(0xFFFAFAFA),
    surfaceColor11 = Color(0xFFF49191),
    surfaceColor12 = Color(0xFFFBFBFB),
    surfaceColor13 = Color(0xFFF4ECFC),
    surfaceColor14 = Color(0xFFF3E8FF),
    surfaceColor15 = Color(0xFFD9D9D9),
    surfaceColor16 = Color(0xFFF7F7F7),
    surfaceColor17 = Color(0xFFECECEC),
    onSurfaceColor1 = Color(0xFF857563),
    onSurfaceColor2 = Color(0xFFD1D1D6),
    onSurfaceColor3 = Color(0xFFFFFFFF),
    onSurfaceColor4 = Color(0xFFB286FD),
    onSurfaceColor5 = Color(0xFF8F8170),
    onSurfaceColor7 = Color(0xFFF49191),
    onSurfaceColor8 = Color(0xFFC7C7C7),
    onSurfaceColor9 = Color(0xFFC2C2C2),
    dimColor = Color(0x33000000),
    transparent = Color(0x00000000),
    systemGrey = Color(0xFFBCBCBC)
)

class SpiritColor(
    surfaceColor1: Color,
    surfaceColor2: Color,
    surfaceColor3: Color,
    surfaceColor4: Color,
    surfaceColor5: Color,
    surfaceColor6: Color,
    surfaceColor7: Color,
    surfaceColor8: Color,
    surfaceColor9: Color,
    surfaceColor10: Color,
    surfaceColor11: Color,
    surfaceColor12: Color,
    surfaceColor13: Color,
    surfaceColor14: Color,
    surfaceColor15: Color,
    surfaceColor16: Color,
    surfaceColor17: Color,
    onSurfaceColor1: Color,
    onSurfaceColor2: Color,
    onSurfaceColor3: Color,
    onSurfaceColor4: Color,
    onSurfaceColor5: Color,
    onSurfaceColor7: Color,
    onSurfaceColor8: Color,
    onSurfaceColor9: Color,
    dimColor: Color,
    transparent: Color,
    systemGrey: Color
) {
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

    var surfaceColor6 by mutableStateOf(surfaceColor6)
        private set

    var surfaceColor7 by mutableStateOf(surfaceColor7)
        private set

    var surfaceColor8 by mutableStateOf(surfaceColor8)
        private set

    var surfaceColor9 by mutableStateOf(surfaceColor9)
        private set

    var surfaceColor10 by mutableStateOf(surfaceColor10)
        private set

    var surfaceColor11 by mutableStateOf(surfaceColor11)
        private set

    var surfaceColor12 by mutableStateOf(surfaceColor12)
        private set

    var surfaceColor13 by mutableStateOf(surfaceColor13)
        private set

    var surfaceColor14 by mutableStateOf(surfaceColor14)
        private set

    var surfaceColor15 by mutableStateOf(surfaceColor15)
        private set

    var surfaceColor16 by mutableStateOf(surfaceColor16)
        private set

    var surfaceColor17 by mutableStateOf(surfaceColor17)
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

    var onSurfaceColor7 by mutableStateOf(onSurfaceColor7)
        private set

    var onSurfaceColor8 by mutableStateOf(onSurfaceColor8)
        private set

    var onSurfaceColor9 by mutableStateOf(onSurfaceColor9)
        private set

    var dimColor by mutableStateOf(dimColor)
        private set

    var transparent by mutableStateOf(transparent)
        private set

    var systemGrey by mutableStateOf(systemGrey)
        private set
}

class SpiritsTodoColor(
    surfaceColor3: Color,
    surfaceColor4: Color,
    onSurfaceColor1: Color,
    onSurfaceColor3: Color,
    onSurfaceColor4: Color,
    onSurfaceColor5: Color,
    onSurfaceColor7: Color,
    onSurfaceColor8: Color,
    onSurfaceColor9: Color,
    onSurfaceColor11: Color,
    onSurfaceColor12: Color,
    mainTextColor: Color,
    white: Color
) {
    var surfaceColor3 by mutableStateOf(surfaceColor3)
        private set

    var surfaceColor4 by mutableStateOf(surfaceColor4)
        private set

    var onSurfaceColor1 by mutableStateOf(onSurfaceColor1)
        private set

    var onSurfaceColor3 by mutableStateOf(onSurfaceColor3)
        private set

    var onSurfaceColor4 by mutableStateOf(onSurfaceColor4)
        private set

    var onSurfaceColor5 by mutableStateOf(onSurfaceColor5)
        private set


    var onSurfaceColor7 by mutableStateOf(onSurfaceColor7)
        private set

    var onSurfaceColor8 by mutableStateOf(onSurfaceColor8)
        private set

    var onSurfaceColor9 by mutableStateOf(onSurfaceColor9)
        private set

    var onSurfaceColor11 by mutableStateOf(onSurfaceColor11)
        private set

    var onSurfaceColor12 by mutableStateOf(onSurfaceColor12)
        private set

    var mainTextColor by mutableStateOf(mainTextColor)
        private set

    var white by mutableStateOf(white)
        private set
}