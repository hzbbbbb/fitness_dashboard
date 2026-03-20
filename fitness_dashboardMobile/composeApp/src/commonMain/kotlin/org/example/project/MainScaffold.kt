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

// ─── Shared App State ──────────────────────────────────────────────────────────
// Owned by MainScaffold (shared Compose layer). All screens read from / write to
// this single state. No duplication in the iOS host layer.

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
    val supplementOptions: List<String> = DEFAULT_SUPPLEMENT_OPTIONS
)

internal fun AppUiState.hasAnyRecord(): Boolean =
    selectedTraining != null || checkedSupplements.isNotEmpty() || savedWeight != null

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
    val today = remember { getCurrentDate() }
    val dateInfo = remember { getDateInfo() }

    ProvideFitBoardPalette(
        themeMode = appState.themeMode
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(FitBoardColors.bgGradientStart, FitBoardColors.bgGradientEnd)
                    )
                )
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
                            today = today
                        )
                        AppScreen.Score -> HealthScoreScreen(
                            state = appState,
                            today = today
                        )
                        AppScreen.Records -> RecordsScreen(
                            state = appState,
                            today = today,
                            onStateChange = { appState = it }
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
    val borderColor = if (selected) {
        FitBoardColors.activeCardBorder
    } else {
        Color.Transparent
    }
    val iconChipColor = if (selected) FitBoardColors.badgeActiveBg else FitBoardColors.badgeInactiveBg
    val iconColor = if (selected) FitBoardColors.badgeActiveText else FitBoardColors.badgeInactiveText
    val labelColor = if (selected) FitBoardColors.textPrimary else FitBoardColors.textSecondary

    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
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
                    .size(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconChipColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = screen.icon,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor
                )
            }
            Text(
                text = screen.label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = labelColor
            )
        }
    }
}
