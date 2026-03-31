package org.example.project

internal const val FIT_BOARD_STORAGE_ROOT = "FitBoard"
internal const val FIT_BOARD_CONFIG_PATH = "$FIT_BOARD_STORAGE_ROOT/config.json"
internal const val FIT_BOARD_RECORDS_DIR = "$FIT_BOARD_STORAGE_ROOT/records"
private const val FIT_BOARD_SCHEMA_VERSION = 1

internal data class StoredAppConfig(
    val schemaVersion: Int = FIT_BOARD_SCHEMA_VERSION,
    val themeMode: String = AppThemeMode.SoftGreen.name,
    val sleepGoalMinutes: Int = 8 * 60,
    val stepGoal: Int = 8000,
    val trainingOptions: List<String> = DEFAULT_TRAINING_OPTIONS,
    val supplementOptions: List<String> = DEFAULT_SUPPLEMENT_OPTIONS,
    val homeVisibleCards: List<String> = HomeSummaryCard.entries.map(HomeSummaryCard::name)
)

internal data class StoredHealthSummary(
    val authorizationState: String = HealthAuthorizationState.Idle.name,
    val statusMessage: String = "等待 Apple 健康授权",
    val todaySteps: Int = 0,
    val hasTodaySteps: Boolean = false,
    val sleepDurationHours: Double = 0.0,
    val hasSleepDuration: Boolean = false,
    val lastUpdatedAt: String = ""
)

internal data class StoredDailyRecord(
    val schemaVersion: Int = FIT_BOARD_SCHEMA_VERSION,
    val date: String,
    val weight: String? = null,
    val selectedTraining: String? = null,
    val selectedSupplements: List<String> = emptyList(),
    val note: String = "",
    val isSaved: Boolean = false,
    val healthSummary: StoredHealthSummary = StoredHealthSummary()
)

internal data class AppPersistenceSnapshot(
    val appState: AppUiState,
    val healthSummary: HealthSummaryUiState
)

internal expect object FitBoardFileStorePlatform {
    fun loadOrCreateConfig(defaultConfig: StoredAppConfig): StoredAppConfig
    fun saveConfig(config: StoredAppConfig): Boolean
    fun loadOrCreateDailyRecord(defaultRecord: StoredDailyRecord): StoredDailyRecord
    fun loadDailyRecordOrNull(dateKey: String): StoredDailyRecord?
    fun saveDailyRecord(record: StoredDailyRecord): Boolean
}

internal object FitBoardFileStore {
    fun loadAppSnapshot(todayKey: String): AppPersistenceSnapshot {
        val storedConfig = FitBoardFileStorePlatform
            .loadOrCreateConfig(StoredAppConfig())
            .sanitize()

        val storedRecord = FitBoardFileStorePlatform
            .loadOrCreateDailyRecord(StoredDailyRecord(date = todayKey))
            .sanitize(todayKey = todayKey, config = storedConfig)

        return AppPersistenceSnapshot(
            appState = storedRecord.toAppUiState(config = storedConfig),
            healthSummary = storedRecord.healthSummary.toUiState()
        )
    }

    fun saveConfig(state: AppUiState): Boolean {
        return FitBoardFileStorePlatform.saveConfig(state.toStoredConfig())
    }

    fun saveTodayRecord(
        todayKey: String,
        state: AppUiState,
        healthSummary: HealthSummaryUiState
    ): Boolean {
        return FitBoardFileStorePlatform.saveDailyRecord(
            state.toStoredDailyRecord(
                todayKey = todayKey,
                healthSummary = healthSummary
            )
        )
    }

    fun loadHistoricalDailyRecords(dateKeys: List<String>): Map<String, StoredDailyRecord> {
        return buildMap {
            dateKeys.distinct().forEach { dateKey ->
                val record = FitBoardFileStorePlatform
                    .loadDailyRecordOrNull(dateKey)
                    ?.sanitizeForHistory(todayKey = dateKey)
                    ?: return@forEach

                put(dateKey, record)
            }
        }
    }
}

internal fun storageDateKey(dateInfo: DateInfo): String =
    buildString {
        append(dateInfo.year.toString().padStart(4, '0'))
        append('-')
        append(dateInfo.month.toString().padStart(2, '0'))
        append('-')
        append(dateInfo.dayOfMonth.toString().padStart(2, '0'))
    }

private fun StoredAppConfig.sanitize(): StoredAppConfig {
    return copy(
        schemaVersion = FIT_BOARD_SCHEMA_VERSION,
        themeMode = parseThemeMode(themeMode).name,
        sleepGoalMinutes = sleepGoalMinutes.coerceIn(0, 24 * 60),
        stepGoal = stepGoal.coerceAtLeast(0),
        trainingOptions = sanitizeOptions(trainingOptions, DEFAULT_TRAINING_OPTIONS),
        supplementOptions = sanitizeOptions(supplementOptions, DEFAULT_SUPPLEMENT_OPTIONS),
        homeVisibleCards = sanitizeHomeVisibleCards(homeVisibleCards)
    )
}

private fun StoredDailyRecord.sanitize(
    todayKey: String,
    config: StoredAppConfig
): StoredDailyRecord {
    return copy(
        schemaVersion = FIT_BOARD_SCHEMA_VERSION,
        date = todayKey,
        weight = weight?.trim()?.takeIf { it.isNotEmpty() },
        selectedTraining = selectedTraining?.takeIf { it in config.trainingOptions },
        selectedSupplements = selectedSupplements
            .map { it.trim() }
            .filter { it.isNotEmpty() && it in config.supplementOptions }
            .distinct(),
        note = note.take(140),
        healthSummary = healthSummary.sanitize()
    )
}

private fun StoredDailyRecord.sanitizeForHistory(todayKey: String): StoredDailyRecord {
    return copy(
        schemaVersion = FIT_BOARD_SCHEMA_VERSION,
        date = todayKey,
        weight = weight?.trim()?.takeIf { it.isNotEmpty() },
        selectedTraining = selectedTraining?.trim()?.takeIf { it.isNotEmpty() },
        selectedSupplements = selectedSupplements
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct(),
        note = note.take(140),
        healthSummary = healthSummary.sanitize()
    )
}

private fun StoredHealthSummary.sanitize(): StoredHealthSummary {
    return copy(
        authorizationState = parseAuthorizationState(authorizationState).name
    )
}

private fun AppUiState.toStoredConfig(): StoredAppConfig {
    return StoredAppConfig(
        themeMode = themeMode.name,
        sleepGoalMinutes = sleepGoalHours * 60 + sleepGoalMinutes,
        stepGoal = stepGoal,
        trainingOptions = sanitizeOptions(trainingOptions, DEFAULT_TRAINING_OPTIONS),
        supplementOptions = sanitizeOptions(supplementOptions, DEFAULT_SUPPLEMENT_OPTIONS),
        homeVisibleCards = homeCardsInDisplayOrder().map(HomeSummaryCard::name)
    )
}

private fun AppUiState.toStoredDailyRecord(
    todayKey: String,
    healthSummary: HealthSummaryUiState
): StoredDailyRecord {
    val normalizedWeight = savedWeight?.trim()?.takeIf { it.isNotEmpty() }
        ?: weightInput.trim().takeIf { it.isNotEmpty() }

    return StoredDailyRecord(
        date = todayKey,
        weight = normalizedWeight,
        selectedTraining = selectedTraining,
        selectedSupplements = checkedSupplements.toList().sorted(),
        note = note.trim(),
        isSaved = true,
        healthSummary = healthSummary.toStoredSummary()
    )
}

private fun StoredDailyRecord.toAppUiState(config: StoredAppConfig): AppUiState {
    val sleepGoalHours = config.sleepGoalMinutes / 60
    val sleepGoalMinutes = config.sleepGoalMinutes % 60

    return AppUiState(
        weightInput = weight.orEmpty(),
        savedWeight = weight,
        selectedTraining = selectedTraining,
        checkedSupplements = selectedSupplements.toSet(),
        note = note,
        isSaved = isSaved,
        themeMode = parseThemeMode(config.themeMode),
        sleepGoalHours = sleepGoalHours,
        sleepGoalMinutes = sleepGoalMinutes,
        stepGoal = config.stepGoal,
        trainingOptions = config.trainingOptions,
        supplementOptions = config.supplementOptions,
        homeVisibleCards = parseHomeVisibleCards(config.homeVisibleCards)
    )
}

private fun HealthSummaryUiState.toStoredSummary(): StoredHealthSummary {
    return StoredHealthSummary(
        authorizationState = authorizationState.name,
        statusMessage = statusMessage,
        todaySteps = todaySteps,
        hasTodaySteps = hasTodaySteps,
        sleepDurationHours = sleepDurationHours,
        hasSleepDuration = hasSleepDuration,
        lastUpdatedAt = lastUpdatedAt
    )
}

internal fun StoredHealthSummary.toUiState(): HealthSummaryUiState {
    return HealthSummaryUiState(
        authorizationState = parseAuthorizationState(authorizationState),
        statusMessage = statusMessage,
        todaySteps = todaySteps,
        hasTodaySteps = hasTodaySteps,
        sleepDurationHours = sleepDurationHours,
        hasSleepDuration = hasSleepDuration,
        lastUpdatedAt = lastUpdatedAt
    )
}

private fun sanitizeOptions(values: List<String>, fallback: List<String>): List<String> {
    val safeValues = values
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

    return if (safeValues.isEmpty()) fallback else safeValues
}

private fun parseThemeMode(name: String): AppThemeMode {
    return AppThemeMode.entries.firstOrNull { it.name == name } ?: AppThemeMode.SoftGreen
}

private fun parseAuthorizationState(name: String): HealthAuthorizationState {
    return HealthAuthorizationState.entries.firstOrNull { it.name == name }
        ?: HealthAuthorizationState.Idle
}

private fun sanitizeHomeVisibleCards(values: List<String>): List<String> {
    val parsed = parseHomeVisibleCards(values)
    return if (parsed.isEmpty()) {
        emptyList()
    } else {
        HomeSummaryCard.entries.filter { it in parsed }.map(HomeSummaryCard::name)
    }
}

private fun parseHomeVisibleCards(values: List<String>): Set<HomeSummaryCard> {
    return values.mapNotNull { name ->
        HomeSummaryCard.entries.firstOrNull { it.name == name }
    }.toSet()
}
