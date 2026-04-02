package org.example.project

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

internal object FitBoardAndroidContextHolder {
    lateinit var appContext: Context
        private set

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }
}

internal actual object FitBoardFileStorePlatform {
    actual fun loadOrCreateConfig(defaultConfig: StoredAppConfig): StoredAppConfig {
        val file = resolveFile(FIT_BOARD_CONFIG_PATH)
        ensureParentDirectory(file)

        if (!file.exists()) {
            saveConfig(defaultConfig)
            return defaultConfig
        }

        return runCatching {
            parseConfig(JSONObject(file.readText()))
        }.getOrElse {
            saveConfig(defaultConfig)
            defaultConfig
        }
    }

    actual fun saveConfig(config: StoredAppConfig): Boolean {
        val file = resolveFile(FIT_BOARD_CONFIG_PATH)
        ensureParentDirectory(file)
        return runCatching {
            file.writeText(config.toJson().toString(2))
        }.isSuccess
    }

    actual fun loadOrCreateDailyRecord(defaultRecord: StoredDailyRecord): StoredDailyRecord {
        val file = resolveFile(recordPath(defaultRecord.date))
        ensureParentDirectory(file)

        if (!file.exists()) {
            saveDailyRecord(defaultRecord)
            return defaultRecord
        }

        return runCatching {
            parseDailyRecord(JSONObject(file.readText()), defaultRecord.date)
        }.getOrElse {
            saveDailyRecord(defaultRecord)
            defaultRecord
        }
    }

    actual fun loadDailyRecordOrNull(dateKey: String): StoredDailyRecord? {
        val file = resolveFile(recordPath(dateKey))
        if (!file.exists()) {
            return null
        }

        return runCatching {
            parseDailyRecord(JSONObject(file.readText()), dateKey)
        }.getOrNull()
    }

    actual fun saveDailyRecord(record: StoredDailyRecord): Boolean {
        val file = resolveFile(recordPath(record.date))
        ensureParentDirectory(file)
        return runCatching {
            file.writeText(record.toJson().toString(2))
        }.isSuccess
    }

    private fun resolveFile(relativePath: String): File {
        return File(FitBoardAndroidContextHolder.appContext.filesDir, relativePath)
    }

    private fun ensureParentDirectory(file: File) {
        file.parentFile?.mkdirs()
    }

    private fun parseConfig(json: JSONObject): StoredAppConfig {
        return StoredAppConfig(
            schemaVersion = json.optInt("schemaVersion", 1),
            themeMode = json.optString("themeMode", AppThemeMode.SoftGreen.name),
            sleepGoalMinutes = json.optInt("sleepGoalMinutes", 8 * 60),
            stepGoal = json.optInt("stepGoal", 8000),
            trainingItems = json.optTrainingItemList("trainingItems"),
            trainingOptions = json.optStringList("trainingOptions"),
            supplementOptions = json.optStringList("supplementOptions"),
            homeCardOrder = json.optStringList("homeCardOrder"),
            homeVisibleCards = json.optStringList("homeVisibleCards")
        )
    }

    private fun parseDailyRecord(json: JSONObject, fallbackDate: String): StoredDailyRecord {
        val healthJson = json.optJSONObject("healthSummary")
        return StoredDailyRecord(
            schemaVersion = json.optInt("schemaVersion", 1),
            date = json.optString("date", fallbackDate),
            weight = json.optStringOrNull("weight"),
            selectedTraining = json.optStringOrNull("selectedTraining"),
            selectedSupplements = json.optStringList("selectedSupplements"),
            note = json.optString("note", ""),
            isSaved = json.optBoolean("isSaved", false),
            sleepScore = json.optDoubleOrNull("sleepScore"),
            stepScore = json.optDoubleOrNull("stepScore"),
            trainingScore = json.optDoubleOrNull("trainingScore"),
            supplementScore = json.optDoubleOrNull("supplementScore"),
            healthScoreTotal = json.optIntOrNull("healthScoreTotal"),
            healthSummary = StoredHealthSummary(
                authorizationState = healthJson?.optString(
                    "authorizationState",
                    HealthAuthorizationState.Idle.name
                ) ?: HealthAuthorizationState.Idle.name,
                statusMessage = healthJson?.optString("statusMessage", "等待 Apple 健康授权")
                    ?: "等待 Apple 健康授权",
                todaySteps = healthJson?.optInt("todaySteps", 0) ?: 0,
                hasTodaySteps = healthJson?.optBoolean("hasTodaySteps", false) ?: false,
                sleepDurationHours = healthJson?.optDouble("sleepDurationHours", 0.0) ?: 0.0,
                hasSleepDuration = healthJson?.optBoolean("hasSleepDuration", false) ?: false,
                workoutType = healthJson?.optString("workoutType", "") ?: "",
                workoutDurationMinutes = healthJson?.optDouble("workoutDurationMinutes", 0.0) ?: 0.0,
                hasWorkout = healthJson?.optBoolean("hasWorkout", false) ?: false,
                workoutStartDateIso = healthJson?.optString("workoutStartDateIso", "") ?: "",
                workoutEndDateIso = healthJson?.optString("workoutEndDateIso", "") ?: "",
                workoutCaloriesKilocalories = healthJson?.optDouble("workoutCaloriesKilocalories", 0.0) ?: 0.0,
                hasWorkoutCalories = healthJson?.optBoolean("hasWorkoutCalories", false) ?: false,
                workoutDistanceKilometers = healthJson?.optDouble("workoutDistanceKilometers", 0.0) ?: 0.0,
                hasWorkoutDistance = healthJson?.optBoolean("hasWorkoutDistance", false) ?: false,
                scorePrimaryWorkoutType = healthJson?.optString("scorePrimaryWorkoutType", "") ?: "",
                scorePrimaryWorkoutDurationMinutes = healthJson?.optDouble("scorePrimaryWorkoutDurationMinutes", 0.0)
                    ?: 0.0,
                scoreAdditionalWorkoutDurationMinutes = healthJson?.optDouble(
                    "scoreAdditionalWorkoutDurationMinutes",
                    0.0
                ) ?: 0.0,
                scoreAdditionalWorkoutsRaw = healthJson?.optString("scoreAdditionalWorkoutsRaw", "") ?: "",
                lastUpdatedAt = healthJson?.optString("lastUpdatedAt", "") ?: ""
            )
        )
    }
}

private fun StoredAppConfig.toJson(): JSONObject {
    return JSONObject().apply {
        put("schemaVersion", schemaVersion)
        put("themeMode", themeMode)
        put("sleepGoalMinutes", sleepGoalMinutes)
        put("stepGoal", stepGoal)
        put("trainingItems", JSONArray(trainingItems.map(StoredTrainingItem::toJson)))
        put("trainingOptions", JSONArray(trainingOptions))
        put("supplementOptions", JSONArray(supplementOptions))
        put("homeCardOrder", JSONArray(homeCardOrder))
        put("homeVisibleCards", JSONArray(homeVisibleCards))
    }
}

private fun StoredDailyRecord.toJson(): JSONObject {
    return JSONObject().apply {
        put("schemaVersion", schemaVersion)
        put("date", date)
        put("weight", weight)
        put("selectedTraining", selectedTraining)
        put("selectedSupplements", JSONArray(selectedSupplements))
        put("note", note)
        put("isSaved", isSaved)
        put("sleepScore", sleepScore)
        put("stepScore", stepScore)
        put("trainingScore", trainingScore)
        put("supplementScore", supplementScore)
        put("healthScoreTotal", healthScoreTotal)
        put(
            "healthSummary",
            JSONObject().apply {
                put("authorizationState", healthSummary.authorizationState)
                put("statusMessage", healthSummary.statusMessage)
                put("todaySteps", healthSummary.todaySteps)
                put("hasTodaySteps", healthSummary.hasTodaySteps)
                put("sleepDurationHours", healthSummary.sleepDurationHours)
                put("hasSleepDuration", healthSummary.hasSleepDuration)
                put("workoutType", healthSummary.workoutType)
                put("workoutDurationMinutes", healthSummary.workoutDurationMinutes)
                put("hasWorkout", healthSummary.hasWorkout)
                put("workoutStartDateIso", healthSummary.workoutStartDateIso)
                put("workoutEndDateIso", healthSummary.workoutEndDateIso)
                put("workoutCaloriesKilocalories", healthSummary.workoutCaloriesKilocalories)
                put("hasWorkoutCalories", healthSummary.hasWorkoutCalories)
                put("workoutDistanceKilometers", healthSummary.workoutDistanceKilometers)
                put("hasWorkoutDistance", healthSummary.hasWorkoutDistance)
                put("scorePrimaryWorkoutType", healthSummary.scorePrimaryWorkoutType)
                put("scorePrimaryWorkoutDurationMinutes", healthSummary.scorePrimaryWorkoutDurationMinutes)
                put(
                    "scoreAdditionalWorkoutDurationMinutes",
                    healthSummary.scoreAdditionalWorkoutDurationMinutes
                )
                put("scoreAdditionalWorkoutsRaw", healthSummary.scoreAdditionalWorkoutsRaw)
                put("lastUpdatedAt", healthSummary.lastUpdatedAt)
            }
        )
    }
}

private fun JSONObject.optStringList(key: String): List<String> {
    val jsonArray = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (index in 0 until jsonArray.length()) {
            val value = jsonArray.optString(index).trim()
            if (value.isNotEmpty()) {
                add(value)
            }
        }
    }
}

private fun JSONObject.optTrainingItemList(key: String): List<StoredTrainingItem> {
    val jsonArray = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (index in 0 until jsonArray.length()) {
            val itemJson = jsonArray.optJSONObject(index) ?: continue
            val name = itemJson.optString("name").trim()
            if (name.isEmpty()) {
                continue
            }

            add(
                StoredTrainingItem(
                    name = name,
                    category = itemJson.optString("category", TrainingCategory.OtherWorkout.storageKey),
                    defaultDurationMinutes = itemJson.optInt(
                        "defaultDurationMinutes",
                        TrainingCategory.OtherWorkout.defaultDurationMinutes
                    )
                )
            )
        }
    }
}

private fun StoredTrainingItem.toJson(): JSONObject {
    return JSONObject().apply {
        put("name", name)
        put("category", category)
        put("defaultDurationMinutes", defaultDurationMinutes)
    }
}

private fun JSONObject.optStringOrNull(key: String): String? {
    if (!has(key) || isNull(key)) {
        return null
    }
    return optString(key).trim().takeIf { it.isNotEmpty() }
}

private fun JSONObject.optIntOrNull(key: String): Int? {
    if (!has(key) || isNull(key)) {
        return null
    }
    return optInt(key)
}

private fun JSONObject.optDoubleOrNull(key: String): Double? {
    if (!has(key) || isNull(key)) {
        return null
    }
    return optDouble(key)
}

private fun recordPath(date: String): String = "$FIT_BOARD_RECORDS_DIR/$date.json"
