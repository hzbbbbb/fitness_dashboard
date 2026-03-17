package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val trainingOptions: List<String> = DEFAULT_TRAINING_OPTIONS,
    val supplementOptions: List<String> = DEFAULT_SUPPLEMENT_OPTIONS
)

internal fun AppUiState.hasAnyRecord(): Boolean =
    selectedTraining != null || checkedSupplements.isNotEmpty() || savedWeight != null

// ─── Screen Enum ───────────────────────────────────────────────────────────────

internal enum class AppScreen(val label: String, val icon: String) {
    Home("首页", "⊡"),
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
                    AppScreen.Home -> DashboardScreen(
                        state = appState,
                        dateInfo = dateInfo,
                        today = today,
                        onStateChange = { appState = it }
                    )
                    AppScreen.Records -> RecordsScreen(
                        state = appState,
                        today = today
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
        AppScreen.entries.forEach { screen ->
            val selected = current == screen
            NavigationBarItem(
                icon = {
                    Text(
                        text = screen.icon,
                        fontSize = 18.sp
                    )
                },
                label = {
                    Text(
                        text = screen.label,
                        fontSize = 11.sp
                    )
                },
                selected = selected,
                onClick = { onSelect(screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FitBoardColors.badgeActiveText,
                    selectedTextColor = FitBoardColors.badgeActiveText,
                    indicatorColor = FitBoardColors.badgeActiveBg,
                    unselectedIconColor = FitBoardColors.badgeInactiveText,
                    unselectedTextColor = FitBoardColors.badgeInactiveText,
                )
            )
        }
    }
}
