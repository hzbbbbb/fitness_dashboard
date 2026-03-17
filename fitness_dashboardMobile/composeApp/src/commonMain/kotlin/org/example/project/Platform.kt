package org.example.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getCurrentDate(): String

/**
 * Minimal date snapshot needed for the heatmap grid.
 * dayOfWeek: 1 = Monday … 7 = Sunday (ISO 8601)
 */
data class DateInfo(
    val year: Int,
    val month: Int,      // 1–12
    val dayOfMonth: Int, // 1–31
    val dayOfWeek: Int   // 1=Mon … 7=Sun
)

expect fun getDateInfo(): DateInfo