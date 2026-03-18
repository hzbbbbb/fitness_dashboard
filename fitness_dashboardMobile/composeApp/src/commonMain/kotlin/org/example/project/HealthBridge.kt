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
    val lastUpdatedAt: String = ""
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
            lastUpdatedAt = lastUpdatedAt
        )
    )
}

internal fun currentHealthSummaryState(): HealthSummaryUiState = HealthSummaryBridge.uiState

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
