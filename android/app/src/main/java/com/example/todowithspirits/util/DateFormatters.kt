package com.example.todowithspirits.util

import java.time.format.DateTimeFormatter
import java.util.Locale

val KoreanDateWithDayFormatter = DateTimeFormatter.ofPattern("yyyy. MM. dd (E)", Locale.KOREAN)
val KoreanMonthDayWithDayFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
val KoreanHourMinuteFormatter = DateTimeFormatter.ofPattern("H시 m분", Locale.KOREAN)

val TimeWheelDueTimeFormatter = DateTimeFormatter.ofPattern("HH : mm")

val NotificationDateFormatter = DateTimeFormatter.ofPattern("MM. dd")