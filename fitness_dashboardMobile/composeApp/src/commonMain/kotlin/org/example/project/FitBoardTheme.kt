package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppThemeMode(
    val title: String,
    val subtitle: String
) {
    SoftGreen("默认模式", "当前淡绿色健康风，继续作为默认主题"),
    White("白色模式", "科技简约风，更干净也更理性"),
    Dark("黑色模式", "意式极简风，黑灰与暖白更高级")
}

internal data class FitBoardPalette(
    val bgGradientStart: Color,
    val bgGradientEnd: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val activeCardBg: Color,
    val activeCardBorder: Color,
    val activeText: Color,
    val inactiveCardBg: Color,
    val inactiveCardBorder: Color,
    val inactiveText: Color,
    val badgeActiveBg: Color,
    val badgeActiveText: Color,
    val badgeInactiveBg: Color,
    val badgeInactiveText: Color,
    val innerPanelBg: Color,
    val innerPanelBorder: Color,
    val buttonGreen: Color,
    val buttonSavedBg: Color,
    val buttonSavedText: Color,
    val circleActiveBg: Color,
    val circleActiveBorder: Color,
    val circleActiveCheck: Color,
    val circleInactiveBg: Color,
    val circleInactiveBorder: Color,
    val countBadgeBg: Color,
    val countBadgeBorder: Color,
    val countBadgeText: Color,
    val weightSavedBadgeBg: Color,
    val heatCellEmpty: Color,
    val heatCellToday: Color,
    val heatCellTodayRecord: Color,
    val heatCellRecord: Color,
    val heatTodayBorder: Color,
    val navBarBg: Color,
    val dangerBg: Color,
    val dangerBorder: Color,
    val dangerText: Color
)

private val defaultPalette = buildFitBoardPalette(
    themeMode = AppThemeMode.SoftGreen
)

private val LocalFitBoardPalette = staticCompositionLocalOf { defaultPalette }

@Composable
internal fun ProvideFitBoardPalette(
    themeMode: AppThemeMode,
    content: @Composable () -> Unit
) {
    val palette = buildFitBoardPalette(themeMode = themeMode)
    CompositionLocalProvider(LocalFitBoardPalette provides palette, content = content)
}

internal object FitBoardColors {
    val bgGradientStart: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.bgGradientStart
    val bgGradientEnd: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.bgGradientEnd
    val cardBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.cardBg
    val cardBorder: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.cardBorder
    val textPrimary: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.textPrimary
    val textSecondary: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.textSecondary
    val textHint: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.textHint
    val activeCardBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.activeCardBg
    val activeCardBorder: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.activeCardBorder
    val activeText: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.activeText
    val inactiveCardBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.inactiveCardBg
    val inactiveCardBorder: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.inactiveCardBorder
    val inactiveText: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.inactiveText
    val badgeActiveBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.badgeActiveBg
    val badgeActiveText: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.badgeActiveText
    val badgeInactiveBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.badgeInactiveBg
    val badgeInactiveText: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.badgeInactiveText
    val innerPanelBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.innerPanelBg
    val innerPanelBorder: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.innerPanelBorder
    val buttonGreen: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.buttonGreen
    val buttonSavedBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.buttonSavedBg
    val buttonSavedText: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.buttonSavedText
    val circleActiveBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.circleActiveBg
    val circleActiveBorder: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.circleActiveBorder
    val circleActiveCheck: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.circleActiveCheck
    val circleInactiveBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.circleInactiveBg
    val circleInactiveBorder: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.circleInactiveBorder
    val countBadgeBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.countBadgeBg
    val countBadgeBorder: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.countBadgeBorder
    val countBadgeText: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.countBadgeText
    val weightSavedBadgeBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.weightSavedBadgeBg
    val heatCellEmpty: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatCellEmpty
    val heatCellToday: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatCellToday
    val heatCellTodayRecord: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatCellTodayRecord
    val heatCellRecord: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatCellRecord
    val heatTodayBorder: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatTodayBorder
    val navBarBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.navBarBg
    val dangerBg: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.dangerBg
    val dangerBorder: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.dangerBorder
    val dangerText: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.dangerText
}

private fun buildFitBoardPalette(
    themeMode: AppThemeMode
): FitBoardPalette {
    return when (themeMode) {
        AppThemeMode.SoftGreen -> FitBoardPalette(
            bgGradientStart = Color(0xFFF7F4EA),
            bgGradientEnd = Color(0xFFEEF5EE),
            cardBg = Color(0xFFF9FBF6),
            cardBorder = Color(0xFFDCE4D5),
            textPrimary = Color(0xFF243127),
            textSecondary = Color(0xFF7D897A),
            textHint = Color(0xFF8C978A),
            activeCardBg = Color(0xFFEDF7EF),
            activeCardBorder = Color(0xFFCBE0CD),
            activeText = Color(0xFF25472D),
            inactiveCardBg = Color(0xFFFFFFFF),
            inactiveCardBorder = Color(0xFFDDE4D9),
            inactiveText = Color(0xFF5F6D5F),
            badgeActiveBg = Color(0xFFDFF0E2),
            badgeActiveText = Color(0xFF3E7248),
            badgeInactiveBg = Color(0xFFF1F3EE),
            badgeInactiveText = Color(0xFF879185),
            innerPanelBg = Color(0xFFF6FAF3),
            innerPanelBorder = Color(0xFFE3EADF),
            buttonGreen = Color(0xFF4A7A56),
            buttonSavedBg = Color(0xFFDFF0E2),
            buttonSavedText = Color(0xFF3E7248),
            circleActiveBg = Color(0xFFDFF0E2),
            circleActiveBorder = Color(0xFFB9D7BC),
            circleActiveCheck = Color(0xFF387147),
            circleInactiveBg = Color(0xFFF7F8F3),
            circleInactiveBorder = Color(0xFFD5DDD2),
            countBadgeBg = Color(0xFFEEF6EC),
            countBadgeBorder = Color(0xFFD8E5D8),
            countBadgeText = Color(0xFF52725A),
            weightSavedBadgeBg = Color(0xFFEEF6EC),
            heatCellEmpty = Color(0xFFE8EEE4),
            heatCellToday = Color(0xFFBED9C1),
            heatCellTodayRecord = Color(0xFF6AAA7A),
            heatCellRecord = Color(0xFF8EC99B),
            heatTodayBorder = Color(0xFF8BC39A),
            navBarBg = Color(0xFFF0F4EC),
            dangerBg = Color(0xFFFFF1F0),
            dangerBorder = Color(0xFFFFCDD0),
            dangerText = Color(0xFFD9534F)
        )

        AppThemeMode.White -> FitBoardPalette(
            bgGradientStart = Color(0xFFF7F9FC),
            bgGradientEnd = Color(0xFFF1F5FA),
            cardBg = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE1E7F0),
            textPrimary = Color(0xFF161C24),
            textSecondary = Color(0xFF667286),
            textHint = Color(0xFF8A94A6),
            activeCardBg = Color(0xFFF3F7FC),
            activeCardBorder = Color(0xFFD6E0EC),
            activeText = Color(0xFF223247),
            inactiveCardBg = Color(0xFFFFFFFF),
            inactiveCardBorder = Color(0xFFE2E8F1),
            inactiveText = Color(0xFF5A667A),
            badgeActiveBg = Color(0xFFEFF4FA),
            badgeActiveText = Color(0xFF587088),
            badgeInactiveBg = Color(0xFFF3F6FA),
            badgeInactiveText = Color(0xFF8994A4),
            innerPanelBg = Color(0xFFF7F9FC),
            innerPanelBorder = Color(0xFFE5EBF3),
            buttonGreen = Color(0xFF5D7287),
            buttonSavedBg = Color(0xFFEFF4FA),
            buttonSavedText = Color(0xFF587088),
            circleActiveBg = Color(0xFFEAF0F6),
            circleActiveBorder = Color(0xFFCFDAE5),
            circleActiveCheck = Color(0xFF587088),
            circleInactiveBg = Color(0xFFF5F7FA),
            circleInactiveBorder = Color(0xFFDCE3EC),
            countBadgeBg = Color(0xFFF3F6FA),
            countBadgeBorder = Color(0xFFE1E7F0),
            countBadgeText = Color(0xFF687C90),
            weightSavedBadgeBg = Color(0xFFF2F5FA),
            heatCellEmpty = Color(0xFFEDF2F7),
            heatCellToday = Color(0xFFD4E0EB),
            heatCellTodayRecord = Color(0xFF7D97AE),
            heatCellRecord = Color(0xFFA8BDD1),
            heatTodayBorder = Color(0xFF90A7BC),
            navBarBg = Color(0xFFF2F5F9),
            dangerBg = Color(0xFFFFF4F3),
            dangerBorder = Color(0xFFF3D2CF),
            dangerText = Color(0xFFC65B53)
        )

        AppThemeMode.Dark -> FitBoardPalette(
            bgGradientStart = Color(0xFF080808),
            bgGradientEnd = Color(0xFF121212),
            cardBg = Color(0xFF161616),
            cardBorder = Color(0xFF252525),
            textPrimary = Color(0xFFF3EEE7),
            textSecondary = Color(0xFFA9A29A),
            textHint = Color(0xFF7D7771),
            activeCardBg = Color(0xFF1D1D1D),
            activeCardBorder = Color(0xFF313131),
            activeText = Color(0xFFF8F3EC),
            inactiveCardBg = Color(0xFF191919),
            inactiveCardBorder = Color(0xFF272727),
            inactiveText = Color(0xFFD0C7BD),
            badgeActiveBg = Color(0xFF242321),
            badgeActiveText = Color(0xFFE3D8CB),
            badgeInactiveBg = Color(0xFF1F1F1F),
            badgeInactiveText = Color(0xFF96908A),
            innerPanelBg = Color(0xFF111111),
            innerPanelBorder = Color(0xFF232323),
            buttonGreen = Color(0xFF72675D),
            buttonSavedBg = Color(0xFF23211E),
            buttonSavedText = Color(0xFFE3D8CB),
            circleActiveBg = Color(0xFF2C2926),
            circleActiveBorder = Color(0xFF3D3935),
            circleActiveCheck = Color(0xFFE3D8CB),
            circleInactiveBg = Color(0xFF202020),
            circleInactiveBorder = Color(0xFF303030),
            countBadgeBg = Color(0xFF1E1E1E),
            countBadgeBorder = Color(0xFF2C2C2C),
            countBadgeText = Color(0xFFB8AEA2),
            weightSavedBadgeBg = Color(0xFF1E1E1E),
            heatCellEmpty = Color(0xFF262626),
            heatCellToday = Color(0xFF322F2B),
            heatCellTodayRecord = Color(0xFF857A70),
            heatCellRecord = Color(0xFF5A5752),
            heatTodayBorder = Color(0xFF8D8177),
            navBarBg = Color(0xFF101010),
            dangerBg = Color(0xFF352322),
            dangerBorder = Color(0xFF5B3533),
            dangerText = Color(0xFFFFB4AE)
        )
    }
}

@Composable
internal fun FitCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, FitBoardColors.cardBorder, RoundedCornerShape(20.dp))
            .background(FitBoardColors.cardBg)
            .padding(16.dp),
        content = content
    )
}

@Composable
internal fun CardLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = FitBoardColors.textSecondary
    )
}

@Composable
internal fun CardTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = FitBoardColors.textPrimary
    )
}
