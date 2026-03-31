@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.example.project

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal actual object FitBoardFileStorePlatform {
    actual fun loadOrCreateConfig(defaultConfig: StoredAppConfig): StoredAppConfig {
        val path = resolvePath(FIT_BOARD_CONFIG_PATH)
        ensureParentDirectory(path)

        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
            saveConfig(defaultConfig)
            return defaultConfig
        }

        return readJsonObject(path)?.let(::parseConfig) ?: run {
            saveConfig(defaultConfig)
            defaultConfig
        }
    }

    actual fun saveConfig(config: StoredAppConfig): Boolean {
        return writeJsonObject(resolvePath(FIT_BOARD_CONFIG_PATH), config.toJsonObject())
    }

    actual fun loadOrCreateDailyRecord(defaultRecord: StoredDailyRecord): StoredDailyRecord {
        val path = resolvePath(recordPath(defaultRecord.date))
        ensureParentDirectory(path)

        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
            saveDailyRecord(defaultRecord)
            return defaultRecord
        }

        return readJsonObject(path)?.let {
            parseDailyRecord(it, defaultRecord.date)
        } ?: run {
            saveDailyRecord(defaultRecord)
            defaultRecord
        }
    }

    actual fun loadDailyRecordOrNull(dateKey: String): StoredDailyRecord? {
        val path = resolvePath(recordPath(dateKey))
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
            return null
        }

        return readJsonObject(path)?.let {
            parseDailyRecord(it, dateKey)
        }
    }

    actual fun saveDailyRecord(record: StoredDailyRecord): Boolean {
        return writeJsonObject(resolvePath(recordPath(record.date)), record.toJsonObject())
    }
}

private fun resolvePath(relativePath: String): String {
    val documentsPath = (NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).firstOrNull() as? String).orEmpty()

    return if (documentsPath.isEmpty()) relativePath else "$documentsPath/$relativePath"
}

private fun ensureParentDirectory(path: String) {
    val parentPath = path.substringBeforeLast('/', missingDelimiterValue = "")
    if (parentPath.isEmpty()) {
        return
    }

    NSFileManager.defaultManager.createDirectoryAtPath(
        path = parentPath,
        withIntermediateDirectories = true,
        attributes = null,
        error = null
    )
}

private fun writeJsonObject(path: String, jsonObject: Map<String, Any?>): Boolean {
    ensureParentDirectory(path)
    val data = NSJSONSerialization.dataWithJSONObject(
        obj = jsonObject,
        options = 0u,
        error = null
    ) ?: return false

    val fileManager = NSFileManager.defaultManager
    if (fileManager.fileExistsAtPath(path)) {
        fileManager.removeItemAtPath(path, error = null)
    }
    return fileManager.createFileAtPath(path, data, null)
}

private fun readJsonObject(path: String): Map<*, *>? {
    val data = NSFileManager.defaultManager.contentsAtPath(path) ?: return null
    return NSJSONSerialization.JSONObjectWithData(
        data = data,
        options = 0u,
        error = null
    ) as? Map<*, *>
}

private fun parseConfig(json: Map<*, *>): StoredAppConfig {
    return StoredAppConfig(
        schemaVersion = json.intValue("schemaVersion", 1),
        themeMode = json.stringValue("themeMode", AppThemeMode.SoftGreen.name),
        sleepGoalMinutes = json.intValue("sleepGoalMinutes", 8 * 60),
        stepGoal = json.intValue("stepGoal", 8000),
        trainingOptions = json.stringList("trainingOptions"),
        supplementOptions = json.stringList("supplementOptions"),
        homeVisibleCards = json.stringList("homeVisibleCards")
    )
}

private fun parseDailyRecord(json: Map<*, *>, fallbackDate: String): StoredDailyRecord {
    val healthJson = json.mapValue("healthSummary")
    return StoredDailyRecord(
        schemaVersion = json.intValue("schemaVersion", 1),
        date = json.stringValue("date", fallbackDate),
        weight = json.stringOrNull("weight"),
        selectedTraining = json.stringOrNull("selectedTraining"),
        selectedSupplements = json.stringList("selectedSupplements"),
        note = json.stringValue("note", ""),
        isSaved = json.boolValue("isSaved", false),
        healthSummary = StoredHealthSummary(
            authorizationState = healthJson?.stringValue(
                "authorizationState",
                HealthAuthorizationState.Idle.name
            ) ?: HealthAuthorizationState.Idle.name,
            statusMessage = healthJson?.stringValue("statusMessage", "等待 Apple 健康授权")
                ?: "等待 Apple 健康授权",
            todaySteps = healthJson?.intValue("todaySteps", 0) ?: 0,
            hasTodaySteps = healthJson?.boolValue("hasTodaySteps", false) ?: false,
            sleepDurationHours = healthJson?.doubleValue("sleepDurationHours", 0.0) ?: 0.0,
            hasSleepDuration = healthJson?.boolValue("hasSleepDuration", false) ?: false,
            workoutType = healthJson?.stringValue("workoutType", "") ?: "",
            workoutDurationMinutes = healthJson?.doubleValue("workoutDurationMinutes", 0.0) ?: 0.0,
            hasWorkout = healthJson?.boolValue("hasWorkout", false) ?: false,
            workoutStartDateIso = healthJson?.stringValue("workoutStartDateIso", "") ?: "",
            workoutEndDateIso = healthJson?.stringValue("workoutEndDateIso", "") ?: "",
            workoutCaloriesKilocalories = healthJson?.doubleValue("workoutCaloriesKilocalories", 0.0) ?: 0.0,
            hasWorkoutCalories = healthJson?.boolValue("hasWorkoutCalories", false) ?: false,
            workoutDistanceKilometers = healthJson?.doubleValue("workoutDistanceKilometers", 0.0) ?: 0.0,
            hasWorkoutDistance = healthJson?.boolValue("hasWorkoutDistance", false) ?: false,
            lastUpdatedAt = healthJson?.stringValue("lastUpdatedAt", "") ?: ""
        )
    )
}

private fun StoredAppConfig.toJsonObject(): Map<String, Any?> {
    return mapOf(
        "schemaVersion" to schemaVersion,
        "themeMode" to themeMode,
        "sleepGoalMinutes" to sleepGoalMinutes,
        "stepGoal" to stepGoal,
        "trainingOptions" to trainingOptions,
        "supplementOptions" to supplementOptions,
        "homeVisibleCards" to homeVisibleCards
    )
}

private fun StoredDailyRecord.toJsonObject(): Map<String, Any?> {
    return mapOf(
        "schemaVersion" to schemaVersion,
        "date" to date,
        "weight" to weight,
        "selectedTraining" to selectedTraining,
        "selectedSupplements" to selectedSupplements,
        "note" to note,
        "isSaved" to isSaved,
        "healthSummary" to mapOf(
            "authorizationState" to healthSummary.authorizationState,
            "statusMessage" to healthSummary.statusMessage,
            "todaySteps" to healthSummary.todaySteps,
            "hasTodaySteps" to healthSummary.hasTodaySteps,
            "sleepDurationHours" to healthSummary.sleepDurationHours,
            "hasSleepDuration" to healthSummary.hasSleepDuration,
            "workoutType" to healthSummary.workoutType,
            "workoutDurationMinutes" to healthSummary.workoutDurationMinutes,
            "hasWorkout" to healthSummary.hasWorkout,
            "workoutStartDateIso" to healthSummary.workoutStartDateIso,
            "workoutEndDateIso" to healthSummary.workoutEndDateIso,
            "workoutCaloriesKilocalories" to healthSummary.workoutCaloriesKilocalories,
            "hasWorkoutCalories" to healthSummary.hasWorkoutCalories,
            "workoutDistanceKilometers" to healthSummary.workoutDistanceKilometers,
            "hasWorkoutDistance" to healthSummary.hasWorkoutDistance,
            "lastUpdatedAt" to healthSummary.lastUpdatedAt
        )
    )
}

private fun Map<*, *>.stringValue(key: String, fallback: String): String {
    return (this[key] as? String)?.trim()?.takeIf { it.isNotEmpty() } ?: fallback
}

private fun Map<*, *>.stringOrNull(key: String): String? {
    return (this[key] as? String)?.trim()?.takeIf { it.isNotEmpty() }
}

private fun Map<*, *>.intValue(key: String, fallback: Int): Int {
    return when (val value = this[key]) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: fallback
        else -> fallback
    }
}

private fun Map<*, *>.doubleValue(key: String, fallback: Double): Double {
    return when (val value = this[key]) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: fallback
        else -> fallback
    }
}

private fun Map<*, *>.boolValue(key: String, fallback: Boolean): Boolean {
    return when (val value = this[key]) {
        is Boolean -> value
        is String -> value.toBooleanStrictOrNull() ?: fallback
        else -> fallback
    }
}

private fun Map<*, *>.stringList(key: String): List<String> {
    val values = this[key] as? List<*> ?: return emptyList()
    return values.mapNotNull { (it as? String)?.trim()?.takeIf(String::isNotEmpty) }
}

private fun Map<*, *>.mapValue(key: String): Map<*, *>? {
    return this[key] as? Map<*, *>
}

private fun recordPath(date: String): String = "$FIT_BOARD_RECORDS_DIR/$date.json"
