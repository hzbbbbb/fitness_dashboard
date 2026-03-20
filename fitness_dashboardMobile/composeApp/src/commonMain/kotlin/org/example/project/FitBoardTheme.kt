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
    SoftGreen("淡绿色模式", "默认主题，沿用当前浅绿与米白风格"),
    White("白色模式", "更干净的浅色界面，层次更克制"),
    Dark("黑色模式", "深灰与暗绿为主的安静深色风格")
}

enum class HeatmapAccent(
    val title: String,
    val subtitle: String
) {
    Green("绿色", "默认健康感绿色"),
    Blue("蓝色", "更冷静的蓝绿色调"),
    Amber("橙色", "柔和偏暖的记录高亮")
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
    themeMode = AppThemeMode.SoftGreen,
    heatmapAccent = HeatmapAccent.Green
)

private val LocalFitBoardPalette = staticCompositionLocalOf { defaultPalette }

@Composable
internal fun ProvideFitBoardPalette(
    themeMode: AppThemeMode,
    heatmapAccent: HeatmapAccent,
    content: @Composable () -> Unit
) {
    val palette = buildFitBoardPalette(themeMode = themeMode, heatmapAccent = heatmapAccent)
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
    themeMode: AppThemeMode,
    heatmapAccent: HeatmapAccent
): FitBoardPalette {
    val accent = accentColorsFor(heatmapAccent = heatmapAccent, dark = themeMode == AppThemeMode.Dark)

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
            badgeActiveText = accent.accentText,
            badgeInactiveBg = Color(0xFFF1F3EE),
            badgeInactiveText = Color(0xFF879185),
            innerPanelBg = Color(0xFFF6FAF3),
            innerPanelBorder = Color(0xFFE3EADF),
            buttonGreen = accent.primaryButton,
            buttonSavedBg = Color(0xFFDFF0E2),
            buttonSavedText = accent.accentText,
            circleActiveBg = accent.softFill,
            circleActiveBorder = accent.softBorder,
            circleActiveCheck = accent.accentText,
            circleInactiveBg = Color(0xFFF7F8F3),
            circleInactiveBorder = Color(0xFFD5DDD2),
            countBadgeBg = Color(0xFFEEF6EC),
            countBadgeBorder = Color(0xFFD8E5D8),
            countBadgeText = accent.mutedText,
            weightSavedBadgeBg = Color(0xFFEEF6EC),
            heatCellEmpty = Color(0xFFE8EEE4),
            heatCellToday = accent.todayFill,
            heatCellTodayRecord = accent.todayRecord,
            heatCellRecord = accent.recordFill,
            heatTodayBorder = accent.recordBorder,
            navBarBg = Color(0xFFF0F4EC),
            dangerBg = Color(0xFFFFF1F0),
            dangerBorder = Color(0xFFFFCDD0),
            dangerText = Color(0xFFD9534F)
        )

        AppThemeMode.White -> FitBoardPalette(
            bgGradientStart = Color(0xFFFAFAF7),
            bgGradientEnd = Color(0xFFF4F6F3),
            cardBg = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE5E8E3),
            textPrimary = Color(0xFF1F2721),
            textSecondary = Color(0xFF748075),
            textHint = Color(0xFF8C968E),
            activeCardBg = Color(0xFFF4F8F4),
            activeCardBorder = Color(0xFFD7E1D8),
            activeText = Color(0xFF213A27),
            inactiveCardBg = Color(0xFFFFFFFF),
            inactiveCardBorder = Color(0xFFE3E7E3),
            inactiveText = Color(0xFF5F695F),
            badgeActiveBg = Color(0xFFF0F4F0),
            badgeActiveText = accent.accentText,
            badgeInactiveBg = Color(0xFFF5F6F4),
            badgeInactiveText = Color(0xFF8A938C),
            innerPanelBg = Color(0xFFF8FAF7),
            innerPanelBorder = Color(0xFFE7EBE5),
            buttonGreen = accent.primaryButton,
            buttonSavedBg = Color(0xFFF0F4F0),
            buttonSavedText = accent.accentText,
            circleActiveBg = accent.softFill,
            circleActiveBorder = accent.softBorder,
            circleActiveCheck = accent.accentText,
            circleInactiveBg = Color(0xFFF7F8F6),
            circleInactiveBorder = Color(0xFFDDE2DC),
            countBadgeBg = Color(0xFFF5F7F4),
            countBadgeBorder = Color(0xFFE1E6E0),
            countBadgeText = accent.mutedText,
            weightSavedBadgeBg = Color(0xFFF4F7F3),
            heatCellEmpty = Color(0xFFECEFEA),
            heatCellToday = accent.todayFill,
            heatCellTodayRecord = accent.todayRecord,
            heatCellRecord = accent.recordFill,
            heatTodayBorder = accent.recordBorder,
            navBarBg = Color(0xFFF4F6F3),
            dangerBg = Color(0xFFFFF4F3),
            dangerBorder = Color(0xFFF3D2CF),
            dangerText = Color(0xFFC65B53)
        )

        AppThemeMode.Dark -> FitBoardPalette(
            bgGradientStart = Color(0xFF111714),
            bgGradientEnd = Color(0xFF171F1B),
            cardBg = Color(0xFF19211D),
            cardBorder = Color(0xFF2A3530),
            textPrimary = Color(0xFFE7EFE7),
            textSecondary = Color(0xFFA4B0A4),
            textHint = Color(0xFF889487),
            activeCardBg = Color(0xFF223028),
            activeCardBorder = Color(0xFF33483D),
            activeText = Color(0xFFE1F1E4),
            inactiveCardBg = Color(0xFF1B2520),
            inactiveCardBorder = Color(0xFF2A3530),
            inactiveText = Color(0xFFC5D0C5),
            badgeActiveBg = Color(0xFF24342B),
            badgeActiveText = accent.accentText,
            badgeInactiveBg = Color(0xFF222D27),
            badgeInactiveText = Color(0xFFA0ACA0),
            innerPanelBg = Color(0xFF151D19),
            innerPanelBorder = Color(0xFF29332E),
            buttonGreen = accent.primaryButton,
            buttonSavedBg = Color(0xFF213028),
            buttonSavedText = accent.accentText,
            circleActiveBg = accent.softFill,
            circleActiveBorder = accent.softBorder,
            circleActiveCheck = accent.accentText,
            circleInactiveBg = Color(0xFF202924),
            circleInactiveBorder = Color(0xFF33403A),
            countBadgeBg = Color(0xFF202B25),
            countBadgeBorder = Color(0xFF313D36),
            countBadgeText = accent.mutedText,
            weightSavedBadgeBg = Color(0xFF202B25),
            heatCellEmpty = Color(0xFF232E28),
            heatCellToday = accent.todayFill,
            heatCellTodayRecord = accent.todayRecord,
            heatCellRecord = accent.recordFill,
            heatTodayBorder = accent.recordBorder,
            navBarBg = Color(0xFF141B18),
            dangerBg = Color(0xFF352322),
            dangerBorder = Color(0xFF5B3533),
            dangerText = Color(0xFFFFB4AE)
        )
    }
}

private data class HeatmapAccentColors(
    val recordFill: Color,
    val todayRecord: Color,
    val todayFill: Color,
    val recordBorder: Color,
    val accentText: Color,
    val mutedText: Color,
    val primaryButton: Color,
    val softFill: Color,
    val softBorder: Color
)

private fun accentColorsFor(
    heatmapAccent: HeatmapAccent,
    dark: Boolean
): HeatmapAccentColors =
    when (heatmapAccent) {
        HeatmapAccent.Green -> if (dark) {
            HeatmapAccentColors(
                recordFill = Color(0xFF427658),
                todayRecord = Color(0xFF5E9A72),
                todayFill = Color(0xFF294436),
                recordBorder = Color(0xFF6AA57D),
                accentText = Color(0xFFA8D8B5),
                mutedText = Color(0xFF8BC39A),
                primaryButton = Color(0xFF5C8A67),
                softFill = Color(0xFF243B2E),
                softBorder = Color(0xFF3B5B48)
            )
        } else {
            HeatmapAccentColors(
                recordFill = Color(0xFF8EC99B),
                todayRecord = Color(0xFF6AAA7A),
                todayFill = Color(0xFFBED9C1),
                recordBorder = Color(0xFF8BC39A),
                accentText = Color(0xFF3E7248),
                mutedText = Color(0xFF52725A),
                primaryButton = Color(0xFF4A7A56),
                softFill = Color(0xFFDFF0E2),
                softBorder = Color(0xFFB9D7BC)
            )
        }

        HeatmapAccent.Blue -> if (dark) {
            HeatmapAccentColors(
                recordFill = Color(0xFF3A6680),
                todayRecord = Color(0xFF4D86A6),
                todayFill = Color(0xFF21394A),
                recordBorder = Color(0xFF5F95B5),
                accentText = Color(0xFFA7CCE2),
                mutedText = Color(0xFF7FB4D2),
                primaryButton = Color(0xFF4C7590),
                softFill = Color(0xFF213746),
                softBorder = Color(0xFF39586C)
            )
        } else {
            HeatmapAccentColors(
                recordFill = Color(0xFF94C3DC),
                todayRecord = Color(0xFF6FA8C7),
                todayFill = Color(0xFFC6DFEC),
                recordBorder = Color(0xFF85B5D1),
                accentText = Color(0xFF43718A),
                mutedText = Color(0xFF4E7288),
                primaryButton = Color(0xFF537A90),
                softFill = Color(0xFFE1EFF6),
                softBorder = Color(0xFFBCD3E1)
            )
        }

        HeatmapAccent.Amber -> if (dark) {
            HeatmapAccentColors(
                recordFill = Color(0xFF8A6138),
                todayRecord = Color(0xFFA87748),
                todayFill = Color(0xFF4A3421),
                recordBorder = Color(0xFFC08C58),
                accentText = Color(0xFFFFD19F),
                mutedText = Color(0xFFE2B37E),
                primaryButton = Color(0xFF8A6A4A),
                softFill = Color(0xFF423022),
                softBorder = Color(0xFF5D4631)
            )
        } else {
            HeatmapAccentColors(
                recordFill = Color(0xFFE0BC8D),
                todayRecord = Color(0xFFC99861),
                todayFill = Color(0xFFF2DEC1),
                recordBorder = Color(0xFFD7AA72),
                accentText = Color(0xFF9A6836),
                mutedText = Color(0xFF8B6948),
                primaryButton = Color(0xFF8A6A4A),
                softFill = Color(0xFFF6E8D4),
                softBorder = Color(0xFFE3C8A4)
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
