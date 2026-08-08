package com.example.todowithspirits.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

val spiritsLightColor = SpiritColor(
    surfaceColor1 = Color(0xFFFFFFFF),
    surfaceColor4 = Color(0xFFF6F6F6),
    surfaceColor5 = Color(0xFF353534),
    surfaceColor6 = Color(0xB3F3E8FF),
    surfaceColor7 = Color(0xFFEAEAEA),
    surfaceColor8 = Color(0xFF8DE4FF),
    surfaceColor9 = Color(0xFFB2F042),
    surfaceColor10 = Color(0xFFFAFAFA),
    surfaceColor12 = Color(0xFFFBFBFB),
    surfaceColor13 = Color(0xFFF4ECFC),
    surfaceColor14 = Color(0xFFF3E8FF),
    surfaceColor15 = Color(0xFFD9D9D9),
    surfaceColor16 = Color(0xFFF7F7F7),
    onSurfaceColor2 = Color(0xFFD1D1D6),
    onSurfaceColor3 = Color(0xFFFFFFFF),
    onSurfaceColor5 = Color(0xFF8F8170),
    onSurfaceColor8 = Color(0xFFC7C7C7),
    onSurfaceColor9 = Color(0xFFC2C2C2),
    onSurfaceColor10 = Color(0xFF6A66A1),
    dimColor = Color(0x33000000),
    transparent = Color(0x00000000),
    systemGrey = Color(0xFFBCBCBC),
    todoTextMain = Color(0xFF857563),
    keyTodo = Color(0xFF8DE4FF),
    keyRoutine = Color(0xFFB2F042),
    systemBackground = Color(0xFFFAFAFA),
    mainTextAndStroke = Color(0xFFB286FD),
    systemArea = Color(0xFFECECEC),
    mainArea = Color(0xFFD7BFFF),
    systemRed = Color(0xFFF49191),
    kakaoBg = Color(0xFFFEE500),
    kakaoText = Color(0xD9000000),
    googleText = Color(0xFF1F1F1F)
)

class SpiritColor(
    surfaceColor1: Color,
    surfaceColor4: Color,
    surfaceColor5: Color,
    surfaceColor6: Color,
    surfaceColor7: Color,
    surfaceColor8: Color,
    surfaceColor9: Color,
    surfaceColor10: Color,
    surfaceColor12: Color,
    surfaceColor13: Color,
    surfaceColor14: Color,
    surfaceColor15: Color,
    surfaceColor16: Color,
    onSurfaceColor2: Color,
    onSurfaceColor3: Color,
    onSurfaceColor5: Color,
    onSurfaceColor8: Color,
    onSurfaceColor9: Color,
    onSurfaceColor10: Color,
    dimColor: Color,
    transparent: Color,
    systemGrey: Color,
    todoTextMain: Color,
    keyTodo: Color,
    keyRoutine: Color,
    systemBackground: Color,
    mainTextAndStroke: Color,
    systemArea: Color,
    mainArea: Color,
    systemRed: Color,
    kakaoBg: Color,
    kakaoText: Color,
    googleText: Color
) {
    var surfaceColor1 by mutableStateOf(surfaceColor1)
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

    var onSurfaceColor2 by mutableStateOf(onSurfaceColor2)
        private set

    var onSurfaceColor3 by mutableStateOf(onSurfaceColor3)
        private set
    var onSurfaceColor5 by mutableStateOf(onSurfaceColor5)
        private set

    var onSurfaceColor8 by mutableStateOf(onSurfaceColor8)
        private set

    var onSurfaceColor9 by mutableStateOf(onSurfaceColor9)
        private set

    var onSurfaceColor10 by mutableStateOf(onSurfaceColor10)
        private set

    var dimColor by mutableStateOf(dimColor)
        private set

    var transparent by mutableStateOf(transparent)
        private set

    var systemGrey by mutableStateOf(systemGrey)
        private set

    var todoTextMain by mutableStateOf(todoTextMain)
        private set

    var keyTodo by mutableStateOf(keyTodo)
        private set

    var keyRoutine by mutableStateOf(keyRoutine)
        private set

    var systemBackground by mutableStateOf(systemBackground)
        private set

    var mainTextAndStroke by mutableStateOf(mainTextAndStroke)
        private set

    var systemArea by mutableStateOf(systemArea)
        private set

    var mainArea by mutableStateOf(mainArea)
        private set

    var systemRed by mutableStateOf(systemRed)
        private set

    var kakaoBg by mutableStateOf(kakaoBg)
        private set

    var kakaoText by mutableStateOf(kakaoText)
        private set

    var googleText by mutableStateOf(googleText)
        private set
}