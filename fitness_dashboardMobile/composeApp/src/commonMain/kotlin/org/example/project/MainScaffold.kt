package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Default Option Lists ──────────────────────────────────────────────────────

internal val DEFAULT_TRAINING_OPTIONS = listOf("胸", "背", "腿", "肩", "手臂", "有氧", "休息")
internal val DEFAULT_SUPPLEMENT_OPTIONS = listOf("蛋白粉", "肌酸", "咖啡因", "鱼油", "维生素")
internal val DEFAULT_HOME_VISIBLE_CARDS = HomeSummaryCard.entries.toSet()

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

// ─── Screen Enum ───────────────────────────────────────────────────────────────

internal enum class AppScreen(val label: String, val icon: String) {
    Home("首页", "⊡"),
    Score("评分", "◎"),
    Records("记录", "☰"),
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
        appState.isSaved,
        healthSummaryState.authorizationState,
        healthSummaryState.statusMessage,
        healthSummaryState.todaySteps,
        healthSummaryState.hasTodaySteps,
        healthSummaryState.sleepDurationHours,
        healthSummaryState.hasSleepDuration,
        healthSummaryState.lastUpdatedAt
    ) {
        if (hasLoadedPersistence && appState.isSaved) {
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
                            onStateChange = { appState = it },
                            onSaveTodayRecord = {
                                val normalizedWeight = appState.weightInput.trim()
                                    .takeIf { it.isNotEmpty() }
                                    ?: appState.savedWeight

                                val stateToSave = appState.copy(
                                    weightInput = normalizedWeight.orEmpty(),
                                    savedWeight = normalizedWeight,
                                    isSaved = true
                                )

                                val didSave = FitBoardFileStore.saveTodayRecord(
                                    todayKey = todayKey,
                                    state = stateToSave,
                                    healthSummary = healthSummaryState
                                )

                                appState = stateToSave.copy(isSaved = didSave)
                            }
                        )
                        AppScreen.Settings -> SettingsScreen(
                            state = appState,
                            onStateChange = { appState = it }
                        )
                    }
                }

                FitBoardBottomNav(
                    current = currentScreen,
                    onSelect = { currentScreen = it }
                )
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
                .padding(horizontal = 10.dp, vertical = 6.dp)
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
    val containerColor = if (selected) FitBoardColors.activeCardBg else Color.Transparent
    val borderColor = if (selected) FitBoardColors.activeCardBorder else Color.Transparent
    val iconChipColor = if (selected) FitBoardColors.innerPanelBg else Color.Transparent
    val iconBorderColor = if (selected) FitBoardColors.innerPanelBorder else Color.Transparent
    val iconColor = if (selected) FitBoardColors.textPrimary else FitBoardColors.textHint
    val labelColor = if (selected) FitBoardColors.textPrimary else FitBoardColors.textSecondary

    Box(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, iconBorderColor, RoundedCornerShape(9.dp))
                    .background(iconChipColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = screen.icon,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor
                )
            }
            Text(
                text = screen.label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = labelColor
            )
        }
    }
}
