package org.example.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.floor
import kotlin.math.roundToInt

enum class HealthAuthorizationState {
    Idle,
    Loading,
    Authorized,
    Denied,
    Unavailable,
    Error
}

data class HealthSummaryUiState(
    val authorizationState: HealthAuthorizationState = HealthAuthorizationState.Idle,
    val statusMessage: String = "等待 Apple 健康授权",
    val todaySteps: Int = 0,
    val hasTodaySteps: Boolean = false,
    val sleepDurationHours: Double = 0.0,
    val hasSleepDuration: Boolean = false,
    val todayWeightKilograms: Double = 0.0,
    val hasTodayWeight: Boolean = false,
    val weightHistoryRaw: String = "",
    val workoutType: String = "",
    val workoutDurationMinutes: Double = 0.0,
    val hasWorkout: Boolean = false,
    val workoutStartDateIso: String = "",
    val workoutEndDateIso: String = "",
    val workoutCaloriesKilocalories: Double = 0.0,
    val hasWorkoutCalories: Boolean = false,
    val workoutDistanceKilometers: Double = 0.0,
    val hasWorkoutDistance: Boolean = false,
    val scorePrimaryWorkoutType: String = "",
    val scorePrimaryWorkoutDurationMinutes: Double = 0.0,
    val scoreAdditionalWorkoutDurationMinutes: Double = 0.0,
    val scoreAdditionalWorkoutsRaw: String = "",
    val lastUpdatedAt: String = ""
)

internal data class WorkoutDisplayEntry(
    val type: String,
    val durationMinutes: Double
)

internal data class WeightHistoryEntry(
    val day: CalendarDay,
    val kilograms: Double
)

object HealthSummaryBridge {
    var uiState by mutableStateOf(HealthSummaryUiState())
        private set

    fun update(newState: HealthSummaryUiState) {
        uiState = newState
    }
}

fun updateHealthIdle(message: String = "等待 Apple 健康授权") {
    HealthSummaryBridge.update(
        HealthSummaryBridge.uiState.copy(
            authorizationState = HealthAuthorizationState.Idle,
            statusMessage = message,
            lastUpdatedAt = ""
        )
    )
}

fun updateHealthLoading(message: String = "正在读取 Apple 健康数据...") {
    HealthSummaryBridge.update(
        HealthSummaryBridge.uiState.copy(
            authorizationState = HealthAuthorizationState.Loading,
            statusMessage = message
        )
    )
}

fun updateHealthUnavailable(message: String) {
    HealthSummaryBridge.update(
        HealthSummaryUiState(
            authorizationState = HealthAuthorizationState.Unavailable,
            statusMessage = message
        )
    )
}

fun updateHealthDenied(message: String) {
    HealthSummaryBridge.update(
        HealthSummaryUiState(
            authorizationState = HealthAuthorizationState.Denied,
            statusMessage = message
        )
    )
}

fun updateHealthError(message: String) {
    HealthSummaryBridge.update(
        HealthSummaryBridge.uiState.copy(
            authorizationState = HealthAuthorizationState.Error,
            statusMessage = message
        )
    )
}

fun updateHealthAuthorized(
    todaySteps: Int,
    hasTodaySteps: Boolean,
    sleepDurationHours: Double,
    hasSleepDuration: Boolean,
    todayWeightKilograms: Double,
    hasTodayWeight: Boolean,
    weightHistoryRaw: String,
    workoutType: String,
    workoutDurationMinutes: Double,
    hasWorkout: Boolean,
    workoutStartDateIso: String,
    workoutEndDateIso: String,
    workoutCaloriesKilocalories: Double,
    hasWorkoutCalories: Boolean,
    workoutDistanceKilometers: Double,
    hasWorkoutDistance: Boolean,
    scorePrimaryWorkoutType: String,
    scorePrimaryWorkoutDurationMinutes: Double,
    scoreAdditionalWorkoutDurationMinutes: Double,
    scoreAdditionalWorkoutsRaw: String,
    lastUpdatedAt: String,
    statusMessage: String = "Apple 健康数据已更新"
) {
    HealthSummaryBridge.update(
        HealthSummaryUiState(
            authorizationState = HealthAuthorizationState.Authorized,
            statusMessage = statusMessage,
            todaySteps = todaySteps,
            hasTodaySteps = hasTodaySteps,
            sleepDurationHours = sleepDurationHours,
            hasSleepDuration = hasSleepDuration,
            todayWeightKilograms = if (hasTodayWeight) {
                todayWeightKilograms.coerceAtLeast(0.0)
            } else {
                0.0
            },
            hasTodayWeight = hasTodayWeight && todayWeightKilograms > 0.0,
            weightHistoryRaw = weightHistoryRaw.trim(),
            workoutType = workoutType,
            workoutDurationMinutes = workoutDurationMinutes,
            hasWorkout = hasWorkout,
            workoutStartDateIso = workoutStartDateIso,
            workoutEndDateIso = workoutEndDateIso,
            workoutCaloriesKilocalories = workoutCaloriesKilocalories,
            hasWorkoutCalories = hasWorkoutCalories,
            workoutDistanceKilometers = workoutDistanceKilometers,
            hasWorkoutDistance = hasWorkoutDistance,
            scorePrimaryWorkoutType = scorePrimaryWorkoutType,
            scorePrimaryWorkoutDurationMinutes = scorePrimaryWorkoutDurationMinutes,
            scoreAdditionalWorkoutDurationMinutes = scoreAdditionalWorkoutDurationMinutes,
            scoreAdditionalWorkoutsRaw = scoreAdditionalWorkoutsRaw,
            lastUpdatedAt = lastUpdatedAt
        )
    )
}

internal fun currentHealthSummaryState(): HealthSummaryUiState = HealthSummaryBridge.uiState

internal fun HealthSummaryUiState.primaryWorkoutDisplayType(): String =
    scorePrimaryWorkoutType.trim().ifEmpty { workoutType.trim() }

internal fun HealthSummaryUiState.primaryWorkoutDisplayDurationMinutes(): Double =
    when {
        scorePrimaryWorkoutDurationMinutes > 0.0 -> scorePrimaryWorkoutDurationMinutes
        else -> workoutDurationMinutes.coerceAtLeast(0.0)
    }

internal fun HealthSummaryUiState.additionalWorkoutEntries(): List<WorkoutDisplayEntry> {
    return scoreAdditionalWorkoutsRaw
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .mapNotNull { line ->
            val parts = line.split('\t', limit = 2)
            val type = parts.firstOrNull()?.trim().orEmpty()
            val durationMinutes = parts.getOrNull(1)?.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
            type.takeIf { it.isNotEmpty() }?.let {
                WorkoutDisplayEntry(type = it, durationMinutes = durationMinutes)
            }
        }
        .toList()
}

internal fun HealthSummaryUiState.additionalWorkoutCount(): Int =
    additionalWorkoutEntries().size

internal fun HealthSummaryUiState.hasMultipleWorkouts(): Boolean =
    additionalWorkoutCount() > 0

internal fun formatWeightKilograms(value: Double): String {
    val roundedTenths = (value * 10.0).roundToInt()
    val integerPart = roundedTenths / 10
    val decimalPart = roundedTenths % 10
    return "$integerPart.$decimalPart"
}

internal fun HealthSummaryUiState.formattedTodayWeightValueOrNull(): String? {
    if (!hasTodayWeight || todayWeightKilograms <= 0.0) {
        return null
    }

    return formatWeightKilograms(todayWeightKilograms)
}

internal fun HealthSummaryUiState.formattedTodayWeightTextOrNull(): String? =
    formattedTodayWeightValueOrNull()?.let { "$it kg" }

internal fun HealthSummaryUiState.weightHistoryEntries(): List<WeightHistoryEntry> {
    return weightHistoryRaw
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .mapNotNull { line ->
            val parts = line.split('\t', limit = 2)
            val day = parseStorageKeyToCalendarDayOrNull(parts.firstOrNull().orEmpty()) ?: return@mapNotNull null
            val kilograms = parts.getOrNull(1)
                ?.toDoubleOrNull()
                ?.takeIf { it > 0.0 }
                ?: return@mapNotNull null
            WeightHistoryEntry(
                day = day,
                kilograms = kilograms
            )
        }
        .associateBy(WeightHistoryEntry::day)
        .values
        .sortedBy(WeightHistoryEntry::day)
}

internal fun formatSleepDuration(hours: Double): String {
    val totalMinutes = (hours * 60.0).roundToInt()
    val hourPart = floor(totalMinutes / 60.0).toInt()
    val minutePart = totalMinutes % 60
    return if (minutePart == 0) {
        "${hourPart}小时"
    } else {
        "${hourPart}小时${minutePart}分"
    }
}
