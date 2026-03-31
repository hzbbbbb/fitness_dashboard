package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ─── Default Option Lists ──────────────────────────────────────────────────────

internal val DEFAULT_SUPPLEMENT_OPTIONS = listOf("蛋白粉", "肌酸", "咖啡因", "鱼油", "维生素")
internal val DEFAULT_HOME_CARD_ORDER = HomeSummaryCard.entries.toList()
internal val DEFAULT_HOME_VISIBLE_CARDS = DEFAULT_HOME_CARD_ORDER.toSet()
private const val RECORD_TEXT_AUTOSAVE_DEBOUNCE_MS = 350L
internal const val TRAINING_DURATION_STEP_MINUTES = 5
internal const val TRAINING_DURATION_MIN_MINUTES = 0
internal const val TRAINING_DURATION_MAX_MINUTES = 180

enum class TrainingCategory(
    val storageKey: String,
    val title: String,
    val defaultDurationMinutes: Int
) {
    TraditionalStrengthTraining(
        storageKey = "traditionalStrengthTraining",
        title = "传统力量训练",
        defaultDurationMinutes = 60
    ),
    OtherWorkout(
        storageKey = "otherWorkout",
        title = "其他训练",
        defaultDurationMinutes = 30
    );

    companion object {
        fun fromStorageKey(storageKey: String): TrainingCategory {
            return entries.firstOrNull { it.storageKey == storageKey } ?: OtherWorkout
        }
    }
}

data class TrainingItemConfig(
    val name: String,
    val category: TrainingCategory,
    val defaultDurationMinutes: Int
)

internal val DEFAULT_TRAINING_ITEMS = listOf(
    TrainingItemConfig("胸", TrainingCategory.TraditionalStrengthTraining, 60),
    TrainingItemConfig("背", TrainingCategory.TraditionalStrengthTraining, 60),
    TrainingItemConfig("腿", TrainingCategory.TraditionalStrengthTraining, 75),
    TrainingItemConfig("肩", TrainingCategory.TraditionalStrengthTraining, 45),
    TrainingItemConfig("手臂", TrainingCategory.TraditionalStrengthTraining, 45),
    TrainingItemConfig("其他训练", TrainingCategory.OtherWorkout, 30)
)
internal val DEFAULT_TRAINING_OPTIONS = DEFAULT_TRAINING_ITEMS.map(TrainingItemConfig::name)

// ─── Shared App State ──────────────────────────────────────────────────────────
// Owned by MainScaffold (shared Compose layer). All screens read from / write to
// this single state. No duplication in the iOS host layer.

enum class HomeSummaryCard(
    val title: String,
    val description: String
) {
    Steps("步数", "显示今日步数摘要"),
    Sleep("睡眠", "显示昨晚睡眠摘要"),
    Score("健康分", "显示今日健康分数"),
    Supplements("补剂摄入情况", "显示今日补剂记录"),
    Training("训练情况", "显示今日训练记录")
}

data class AppUiState(
    val weightInput: String = "",
    val savedWeight: String? = null,
    val selectedTraining: String? = null,
    val checkedSupplements: Set<String> = emptySet(),
    val note: String = "",
    val isSaved: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SoftGreen,
    val sleepGoalHours: Int = 8,
    val sleepGoalMinutes: Int = 0,
    val stepGoal: Int = 8000,
    val trainingItems: List<TrainingItemConfig> = DEFAULT_TRAINING_ITEMS,
    val supplementOptions: List<String> = DEFAULT_SUPPLEMENT_OPTIONS,
    val homeCardOrder: List<HomeSummaryCard> = DEFAULT_HOME_CARD_ORDER,
    val homeVisibleCards: Set<HomeSummaryCard> = DEFAULT_HOME_VISIBLE_CARDS
) {
    val trainingOptions: List<String>
        get() = trainingItems.map(TrainingItemConfig::name)
}

internal fun AppUiState.hasAnyRecord(): Boolean =
    selectedTraining != null || checkedSupplements.isNotEmpty() || savedWeight != null || note.isNotBlank()

internal fun AppUiState.homeCardsInDisplayOrder(): List<HomeSummaryCard> =
    orderedHomeCards().filter { it in homeVisibleCards }

internal fun AppUiState.orderedHomeCards(): List<HomeSummaryCard> {
    val normalizedOrder = homeCardOrder
        .distinct()
        .filter { it in HomeSummaryCard.entries }

    if (normalizedOrder.size == HomeSummaryCard.entries.size) {
        return normalizedOrder
    }

    return normalizedOrder + HomeSummaryCard.entries.filter { it !in normalizedOrder }
}

internal fun AppUiState.moveHomeCard(card: HomeSummaryCard, offset: Int): AppUiState {
    val orderedCards = orderedHomeCards()
    val currentIndex = orderedCards.indexOf(card)
    if (currentIndex == -1) {
        return this
    }

    val targetIndex = (currentIndex + offset).coerceIn(0, orderedCards.lastIndex)
    if (targetIndex == currentIndex) {
        return this
    }

    val updatedCards = orderedCards.toMutableList().apply {
        removeAt(currentIndex)
        add(targetIndex, card)
    }
    return copy(homeCardOrder = updatedCards)
}

internal fun AppUiState.trainingItemsIn(category: TrainingCategory): List<TrainingItemConfig> =
    trainingItems.filter { it.category == category }

internal fun AppUiState.defaultTrainingDurationFor(name: String?): Int? =
    trainingItems.firstOrNull { it.name == name }?.defaultDurationMinutes

internal fun AppUiState.withAutoSavedWeight(weightInput: String): AppUiState {
    val normalizedWeight = weightInput.trim().takeIf { it.isNotEmpty() }
    return copy(
        weightInput = weightInput,
        savedWeight = normalizedWeight
    )
}

internal fun normalizeTrainingDurationMinutes(value: Int): Int {
    val safeValue = value.coerceIn(
        minimumValue = TRAINING_DURATION_MIN_MINUTES,
        maximumValue = TRAINING_DURATION_MAX_MINUTES
    )
    return ((safeValue + TRAINING_DURATION_STEP_MINUTES / 2) / TRAINING_DURATION_STEP_MINUTES) *
        TRAINING_DURATION_STEP_MINUTES
}

internal fun inferTrainingCategory(name: String): TrainingCategory {
    val normalized = name.trim()
    if (normalized.isEmpty()) {
        return TrainingCategory.OtherWorkout
    }

    if (normalized.contains("传统力量训练", ignoreCase = true)) {
        return TrainingCategory.TraditionalStrengthTraining
    }

    if (normalized.contains("功能性力量训练", ignoreCase = true)) {
        return TrainingCategory.OtherWorkout
    }

    val strengthKeywords = listOf(
        "胸",
        "背",
        "腿",
        "肩",
        "手臂",
        "二头",
        "三头",
        "臀",
        "核心"
    )

    if (strengthKeywords.any { normalized.contains(it, ignoreCase = true) }) {
        return TrainingCategory.TraditionalStrengthTraining
    }

    return if (normalized.contains("力量", ignoreCase = true) &&
        !normalized.contains("传统力量训练", ignoreCase = true)
    ) {
        TrainingCategory.TraditionalStrengthTraining
    } else {
        TrainingCategory.OtherWorkout
    }
}

internal fun defaultTrainingDurationFor(
    name: String,
    category: TrainingCategory = inferTrainingCategory(name)
): Int {
    val normalized = name.trim()
    val predefined = when {
        normalized == "胸" || normalized.contains("胸") -> 60
        normalized == "背" || normalized.contains("背") -> 60
        normalized == "腿" || normalized.contains("腿") -> 75
        normalized == "肩" || normalized.contains("肩") -> 45
        normalized == "手臂" || normalized.contains("手臂") ||
            normalized.contains("二头") || normalized.contains("三头") -> 45
        normalized.contains("传统力量训练", ignoreCase = true) -> 60
        normalized.contains("跑", ignoreCase = true) -> 30
        normalized.contains("骑", ignoreCase = true) -> 45
        normalized.contains("游泳", ignoreCase = true) -> 30
        normalized.contains("HIIT", ignoreCase = true) -> 20
        normalized.contains("步行", ignoreCase = true) ||
            normalized.contains("走路", ignoreCase = true) -> 30
        normalized.contains("功能性力量训练", ignoreCase = true) -> 45
        normalized.contains("有氧", ignoreCase = true) -> 30
        normalized.contains("休息", ignoreCase = true) -> 0
        normalized.contains("其他训练", ignoreCase = true) -> 30
        else -> category.defaultDurationMinutes
    }

    return normalizeTrainingDurationMinutes(predefined)
}

internal fun sanitizeTrainingItems(
    values: List<TrainingItemConfig>,
    fallback: List<TrainingItemConfig> = DEFAULT_TRAINING_ITEMS
): List<TrainingItemConfig> {
    val safeValues = values
        .mapNotNull { item ->
            val safeName = item.name.trim().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            TrainingItemConfig(
                name = safeName,
                category = item.category,
                defaultDurationMinutes = normalizeTrainingDurationMinutes(item.defaultDurationMinutes)
            )
        }
        .distinctBy(TrainingItemConfig::name)

    if (safeValues.isEmpty()) {
        return fallback
    }

    val safeStrengthItems = safeValues
        .filter { it.category == TrainingCategory.TraditionalStrengthTraining }
        .ifEmpty { fallback.filter { it.category == TrainingCategory.TraditionalStrengthTraining } }

    val otherDurationMinutes = safeValues
        .firstOrNull { it.category == TrainingCategory.OtherWorkout && it.name == "其他训练" }
        ?.defaultDurationMinutes
        ?: safeValues
            .firstOrNull { it.category == TrainingCategory.OtherWorkout }
            ?.defaultDurationMinutes
        ?: fallback
            .firstOrNull { it.category == TrainingCategory.OtherWorkout }
            ?.defaultDurationMinutes
        ?: TrainingCategory.OtherWorkout.defaultDurationMinutes

    return safeStrengthItems + TrainingItemConfig(
        name = "其他训练",
        category = TrainingCategory.OtherWorkout,
        defaultDurationMinutes = normalizeTrainingDurationMinutes(otherDurationMinutes)
    )
}

// ─── Screen Enum ───────────────────────────────────────────────────────────────

internal enum class AppScreen(val label: String, val icon: String) {
    Home("首页", "⌂"),
    Score("评分", "◔"),
    Records("记录", "✎"),
    Settings("设置", "⚙")
}

// ─── Root Composable ───────────────────────────────────────────────────────────

@Composable
fun MainScaffold() {
    var currentScreen by remember { mutableStateOf(AppScreen.Home) }
    var appState by remember { mutableStateOf(AppUiState()) }
    val dateInfo = remember { getDateInfo() }
    val today = remember { getCurrentDate() }
    val todayKey = remember(dateInfo) { storageDateKey(dateInfo) }
    val healthSummaryState = currentHealthSummaryState()
    var hasLoadedPersistence by remember { mutableStateOf(false) }

    LaunchedEffect(todayKey) {
        val snapshot = FitBoardFileStore.loadAppSnapshot(todayKey)
        appState = snapshot.appState
        HealthSummaryBridge.update(snapshot.healthSummary)
        hasLoadedPersistence = true
    }

    LaunchedEffect(
        hasLoadedPersistence,
        appState.themeMode,
        appState.sleepGoalHours,
        appState.sleepGoalMinutes,
        appState.stepGoal,
        appState.trainingItems,
        appState.supplementOptions,
        appState.homeCardOrder,
        appState.homeVisibleCards
    ) {
        if (hasLoadedPersistence) {
            FitBoardFileStore.saveConfig(appState)
        }
    }

    LaunchedEffect(
        hasLoadedPersistence,
        todayKey,
        appState.selectedTraining,
        appState.checkedSupplements,
        healthSummaryState.authorizationState,
        healthSummaryState.statusMessage,
        healthSummaryState.todaySteps,
        healthSummaryState.hasTodaySteps,
        healthSummaryState.sleepDurationHours,
        healthSummaryState.hasSleepDuration,
        healthSummaryState.workoutType,
        healthSummaryState.workoutDurationMinutes,
        healthSummaryState.hasWorkout,
        healthSummaryState.workoutStartDateIso,
        healthSummaryState.workoutEndDateIso,
        healthSummaryState.workoutCaloriesKilocalories,
        healthSummaryState.hasWorkoutCalories,
        healthSummaryState.workoutDistanceKilometers,
        healthSummaryState.hasWorkoutDistance,
        healthSummaryState.scorePrimaryWorkoutType,
        healthSummaryState.scorePrimaryWorkoutDurationMinutes,
        healthSummaryState.scoreAdditionalWorkoutDurationMinutes,
        healthSummaryState.scoreAdditionalWorkoutsRaw,
        healthSummaryState.lastUpdatedAt
    ) {
        if (hasLoadedPersistence) {
            FitBoardFileStore.saveTodayRecord(
                todayKey = todayKey,
                state = appState,
                healthSummary = healthSummaryState
            )
        }
    }

    LaunchedEffect(
        hasLoadedPersistence,
        todayKey,
        appState.weightInput,
        appState.note
    ) {
        if (hasLoadedPersistence) {
            delay(RECORD_TEXT_AUTOSAVE_DEBOUNCE_MS)
            FitBoardFileStore.saveTodayRecord(
                todayKey = todayKey,
                state = appState,
                healthSummary = healthSummaryState
            )
        }
    }

    ProvideFitBoardPalette(
        themeMode = appState.themeMode
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(FitBoardColors.bgGradientStart, FitBoardColors.bgGradientEnd)))
        ) {
            // Column layout: content takes all vertical space above the nav bar.
            // NavigationBar's windowInsets handles the iOS home indicator internally,
            // so screen content naturally stops above the physical bar area.
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when (currentScreen) {
                        AppScreen.Home -> HomeScreen(
                            state = appState,
                            dateInfo = dateInfo,
                            today = today,
                            onStateChange = { appState = it }
                        )
                        AppScreen.Score -> HealthScoreScreen(
                            state = appState,
                            today = today
                        )
                        AppScreen.Records -> RecordsScreen(
                            state = appState,
                            healthState = healthSummaryState,
                            today = today,
                            onWeightChange = { input ->
                                appState = appState.withAutoSavedWeight(input)
                            },
                            onTrainingChange = { training ->
                                appState = appState.copy(selectedTraining = training)
                            },
                            onSupplementsChange = { supplements ->
                                appState = appState.copy(checkedSupplements = supplements)
                            },
                            onNoteChange = { note ->
                                appState = appState.copy(note = note)
                            }
                        )
                        AppScreen.Settings -> SettingsScreen(
                            state = appState,
                            onStateChange = { appState = it }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FitBoardColors.navBarBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(FitBoardColors.cardBorder)
                    )

                    FitBoardBottomNav(
                        current = currentScreen,
                        onSelect = { currentScreen = it }
                    )
                }
            }
        }
    }
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

@Composable
private fun FitBoardBottomNav(
    current: AppScreen,
    onSelect: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = FitBoardColors.navBarBg,
        windowInsets = WindowInsets.navigationBars,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppScreen.entries.forEach { screen ->
                FitBoardBottomNavItem(
                    screen = screen,
                    selected = current == screen,
                    onClick = { onSelect(screen) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FitBoardBottomNavItem(
    screen: AppScreen,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor = if (selected) FitBoardColors.buttonGreen else FitBoardColors.textHint
    val labelColor = if (selected) FitBoardColors.buttonGreen else FitBoardColors.textSecondary
    val indicatorColor = if (selected) FitBoardColors.buttonGreen else Color.Transparent

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(if (selected) 22.dp else 12.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Text(
                text = screen.icon,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = iconColor
            )
            Text(
                text = screen.label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = labelColor
            )
        }
    }
}
