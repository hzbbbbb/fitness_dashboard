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

internal val DEFAULT_TRAINING_OPTIONS = listOf("胸", "背", "腿", "肩", "手臂", "有氧", "休息")
internal val DEFAULT_SUPPLEMENT_OPTIONS = listOf("蛋白粉", "肌酸", "咖啡因", "鱼油", "维生素")
internal val DEFAULT_HOME_VISIBLE_CARDS = HomeSummaryCard.entries.toSet()
private const val RECORD_TEXT_AUTOSAVE_DEBOUNCE_MS = 350L

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
    val trainingOptions: List<String> = DEFAULT_TRAINING_OPTIONS,
    val supplementOptions: List<String> = DEFAULT_SUPPLEMENT_OPTIONS,
    val homeVisibleCards: Set<HomeSummaryCard> = DEFAULT_HOME_VISIBLE_CARDS
)

internal fun AppUiState.hasAnyRecord(): Boolean =
    selectedTraining != null || checkedSupplements.isNotEmpty() || savedWeight != null || note.isNotBlank()

internal fun AppUiState.homeCardsInDisplayOrder(): List<HomeSummaryCard> =
    HomeSummaryCard.entries.filter { it in homeVisibleCards }

internal fun AppUiState.withAutoSavedWeight(weightInput: String): AppUiState {
    val normalizedWeight = weightInput.trim().takeIf { it.isNotEmpty() }
    return copy(
        weightInput = weightInput,
        savedWeight = normalizedWeight
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
        appState.trainingOptions,
        appState.supplementOptions,
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
