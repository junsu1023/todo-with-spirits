package com.example.todowithspirits.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

val spiritsLightColor = SpiritColor(
    white = Color(0xFFFFFFFF),
    back01 = Color(0xFFFDFBFA),
    back02 = Color(0xFFF8F4F1),
    stroke01 = Color(0xFFECEAE4),
    font01 = Color(0xFF857563),
    font02 = Color(0xFFCFB6A0),
    fontGrey = Color(0xFFC9C9C2),
    key01B = Color(0xFFA3B90E),
    key01 = Color(0xFFC1DD00),
    key01Op = Color(0xFFEDFCB7),
    key02B = Color(0xFFF168C6),
    key02 = Color(0xFFFF96DE),
    key02Op = Color(0xFFFFE6F6),
    colorSub01 = Color(0xFF76D8F6),
    colorSub02 = Color(0xFFF79090),

    surfaceColor1 = Color(0xFFFFFFFF),
    surfaceColor4 = Color(0xFFF6F6F6),
    surfaceColor5 = Color(0xFF353534),
    surfaceColor10 = Color(0xFFFAFAFA),
    surfaceColor13 = Color(0xFFF4ECFC),
    surfaceColor15 = Color(0xFFD9D9D9),
    surfaceColor16 = Color(0xFFF7F7F7),
    surfaceColor17 = Color(0xFFF1F1F1),
    onSurfaceColor1 = Color(0xFF9FA8B7),
    onSurfaceColor2 = Color(0xFFD1D1D6),
    onSurfaceColor3 = Color(0xFFFFFFFF),
    onSurfaceColor5 = Color(0xFF8F8170),
    onSurfaceColor8 = Color(0xFFC7C7C7),
    onSurfaceColor9 = Color(0xFFC2C2C2),
    dimColor = Color(0x33000000),
    transparent = Color(0x00000000),
    systemGrey = Color(0xFFBCBCBC),
    todoTextMain = Color(0xFF857563),
    mainBackground = Color(0xFFF3E8FF),
    systemBackground = Color(0xFFFAFAFA),
    mainTextAndStroke = Color(0xFFB286FD),
    systemArea = Color(0xFFECECEC),
    mainArea = Color(0xFFD7BFFF),
    systemRed = Color(0xFFF49191),
    pointPink = Color(0xFFEFA4C6),
    kakaoBg = Color(0xFFFEE500),
    kakaoText = Color(0xD9000000),
    googleText = Color(0xFF1F1F1F)
)

class SpiritColor(
    white: Color,
    back01: Color,
    back02: Color,
    stroke01: Color,
    font01: Color,
    font02: Color,
    fontGrey: Color,
    key01B: Color,
    key01: Color,
    key01Op: Color,
    key02B: Color,
    key02: Color,
    key02Op: Color,
    colorSub01: Color,
    colorSub02: Color,

    surfaceColor1: Color,
    surfaceColor4: Color,
    surfaceColor5: Color,
    surfaceColor10: Color,
    surfaceColor13: Color,
    surfaceColor15: Color,
    surfaceColor16: Color,
    surfaceColor17: Color,
    onSurfaceColor1: Color,
    onSurfaceColor2: Color,
    onSurfaceColor3: Color,
    onSurfaceColor5: Color,
    onSurfaceColor8: Color,
    onSurfaceColor9: Color,
    dimColor: Color,
    transparent: Color,
    systemGrey: Color,
    todoTextMain: Color,
    mainBackground: Color,
    systemBackground: Color,
    mainTextAndStroke: Color,
    systemArea: Color,
    mainArea: Color,
    systemRed: Color,
    pointPink: Color,
    kakaoBg: Color,
    kakaoText: Color,
    googleText: Color
) {
    var white by mutableStateOf(white)
        private set

    var back01 by mutableStateOf(back01)
        private set

    var back02 by mutableStateOf(back02)
        private set

    var stroke01 by mutableStateOf(stroke01)
        private set

    var font01 by mutableStateOf(font01)
        private set

    var font02 by mutableStateOf(font02)
        private set

    var fontGrey by mutableStateOf(fontGrey)
        private set

    var key01B by mutableStateOf(key01B)
        private set

    var key01 by mutableStateOf(key01)
        private set

    var key01Op by mutableStateOf(key01Op)
        private set

    var key02B by mutableStateOf(key02B)
        private set

    var key02 by mutableStateOf(key02)
        private set

    var key02Op by mutableStateOf(key02Op)
        private set

    var colorSub01 by mutableStateOf(colorSub01)
        private set

    var colorSub02 by mutableStateOf(colorSub02)
        private set


    var surfaceColor1 by mutableStateOf(surfaceColor1)
        private set

    var surfaceColor4 by mutableStateOf(surfaceColor4)
        private set

    var surfaceColor5 by mutableStateOf(surfaceColor5)
        private set

    var surfaceColor10 by mutableStateOf(surfaceColor10)
        private set

    var surfaceColor13 by mutableStateOf(surfaceColor13)
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
    var onSurfaceColor5 by mutableStateOf(onSurfaceColor5)
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

    var todoTextMain by mutableStateOf(todoTextMain)
        private set

    var mainBackground by mutableStateOf(mainBackground)
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

    var pointPink by mutableStateOf(pointPink)
        private set

    var kakaoBg by mutableStateOf(kakaoBg)
        private set

    var kakaoText by mutableStateOf(kakaoText)
        private set

    var googleText by mutableStateOf(googleText)
        private set
}