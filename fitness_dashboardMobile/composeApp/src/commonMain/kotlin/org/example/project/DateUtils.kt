package org.example.project

internal data class CalendarDay(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int
) : Comparable<CalendarDay> {
    override fun compareTo(other: CalendarDay): Int {
        return epochDay().compareTo(other.epochDay())
    }
}

internal fun DateInfo.toCalendarDay(): CalendarDay =
    CalendarDay(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth
    )

internal fun CalendarDay.toStorageKey(): String =
    buildString {
        append(year.toString().padStart(4, '0'))
        append('-')
        append(month.toString().padStart(2, '0'))
        append('-')
        append(dayOfMonth.toString().padStart(2, '0'))
    }

internal fun parseStorageKeyToCalendarDayOrNull(value: String): CalendarDay? {
    val parts = value.trim().split('-')
    if (parts.size != 3) {
        return null
    }

    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val dayOfMonth = parts[2].toIntOrNull() ?: return null
    if (month !in 1..12 || dayOfMonth !in 1..31) {
        return null
    }

    return CalendarDay(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth
    )
}

internal fun CalendarDay.plusDays(days: Int): CalendarDay =
    civilFromEpochDay(epochDay() + days)

internal fun CalendarDay.minusDays(days: Int): CalendarDay =
    plusDays(-days)

internal fun CalendarDay.dayOfWeekIso(): Int =
    floorMod(epochDay() + 3, 7) + 1

internal fun CalendarDay.startOfWeekMonday(): CalendarDay =
    plusDays(1 - dayOfWeekIso())

internal fun CalendarDay.daysUntil(other: CalendarDay): Int =
    other.epochDay() - epochDay()

internal fun CalendarDay.formatMonthDayText(): String =
    buildString {
        append(month.toString().padStart(2, '0'))
        append('/')
        append(dayOfMonth.toString().padStart(2, '0'))
    }

private fun CalendarDay.epochDay(): Int =
    daysFromCivil(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth
    )

private fun daysFromCivil(
    year: Int,
    month: Int,
    dayOfMonth: Int
): Int {
    val adjustedYear = year - if (month <= 2) 1 else 0
    val era = floorDiv(adjustedYear, 400)
    val yearOfEra = adjustedYear - era * 400
    val adjustedMonth = month + if (month > 2) -3 else 9
    val dayOfYear = (153 * adjustedMonth + 2) / 5 + dayOfMonth - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146097 + dayOfEra - 719468
}

private fun civilFromEpochDay(epochDay: Int): CalendarDay {
    val shiftedDay = epochDay + 719468
    val era = floorDiv(shiftedDay, 146097)
    val dayOfEra = shiftedDay - era * 146097
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    val year = yearOfEra + era * 400
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthPrime = (5 * dayOfYear + 2) / 153
    val day = dayOfYear - (153 * monthPrime + 2) / 5 + 1
    val month = monthPrime + if (monthPrime < 10) 3 else -9
    val adjustedYear = year + if (month <= 2) 1 else 0

    return CalendarDay(
        year = adjustedYear,
        month = month,
        dayOfMonth = day
    )
}

private fun floorDiv(value: Int, divisor: Int): Int {
    val quotient = value / divisor
    val remainder = value % divisor
    return if (remainder != 0 && (remainder < 0) != (divisor < 0)) quotient - 1 else quotient
}

private fun floorMod(value: Int, divisor: Int): Int {
    val remainder = value % divisor
    return if (remainder < 0) remainder + divisor else remainder
}
