package com.example.todowithspirits.util

import java.time.format.DateTimeFormatter
import java.util.Locale

val KoreanDateWithDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy. MM. dd (E)", Locale.KOREAN)
