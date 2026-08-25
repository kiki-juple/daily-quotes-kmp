package com.disheveled.dailyquotes.ui.util

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun todayInIndonesian(
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String = formatIndonesianDate(clock.now().toLocalDateTime(timeZone).date)

/** Compact form for list rows, e.g. "25 Agu". */
@OptIn(ExperimentalTime::class)
fun formatSavedAt(
    epochMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date
    return "${date.day} ${shortMonth(date.month)}"
}

private fun shortMonth(month: Month): String = when (month) {
    Month.JANUARY -> "Jan"
    Month.FEBRUARY -> "Feb"
    Month.MARCH -> "Mar"
    Month.APRIL -> "Apr"
    Month.MAY -> "Mei"
    Month.JUNE -> "Jun"
    Month.JULY -> "Jul"
    Month.AUGUST -> "Agu"
    Month.SEPTEMBER -> "Sep"
    Month.OCTOBER -> "Okt"
    Month.NOVEMBER -> "Nov"
    Month.DECEMBER -> "Des"
}

fun formatIndonesianDate(date: LocalDate): String {
    val day = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Senin"
        DayOfWeek.TUESDAY -> "Selasa"
        DayOfWeek.WEDNESDAY -> "Rabu"
        DayOfWeek.THURSDAY -> "Kamis"
        DayOfWeek.FRIDAY -> "Jumat"
        DayOfWeek.SATURDAY -> "Sabtu"
        DayOfWeek.SUNDAY -> "Minggu"
    }
    val month = when (date.month) {
        Month.JANUARY -> "Januari"
        Month.FEBRUARY -> "Februari"
        Month.MARCH -> "Maret"
        Month.APRIL -> "April"
        Month.MAY -> "Mei"
        Month.JUNE -> "Juni"
        Month.JULY -> "Juli"
        Month.AUGUST -> "Agustus"
        Month.SEPTEMBER -> "September"
        Month.OCTOBER -> "Oktober"
        Month.NOVEMBER -> "November"
        Month.DECEMBER -> "Desember"
    }
    return "$day, ${date.day} $month"
}
