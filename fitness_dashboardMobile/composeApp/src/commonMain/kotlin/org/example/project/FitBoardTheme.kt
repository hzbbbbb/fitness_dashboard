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
    SoftGreen("默认模式", "白底、黑字、浅灰分层的系统摘要风"),
    White("冷白模式", "更冷静的浅雾白科技风"),
    Dark("深色模式", "深色高对比的摘要风格")
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
    val heatCellLevel1: Color,
    val heatCellLevel2: Color,
    val heatCellLevel3: Color,
    val heatCellLevel4: Color,
    val heatCellLevel5: Color,
    val heatCellBorder: Color,
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
    val heatCellLevel1: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatCellLevel1
    val heatCellLevel2: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatCellLevel2
    val heatCellLevel3: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatCellLevel3
    val heatCellLevel4: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatCellLevel4
    val heatCellLevel5: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatCellLevel5
    val heatCellBorder: Color
        @Composable @ReadOnlyComposable get() = LocalFitBoardPalette.current.heatCellBorder
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
            bgGradientStart = Color(0xFFFFFFFF),
            bgGradientEnd = Color(0xFFF9F9FB),
            cardBg = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE7E8EC),
            textPrimary = Color(0xFF111216),
            textSecondary = Color(0xFF5E636C),
            textHint = Color(0xFF8A9099),
            activeCardBg = Color(0xFFF2F6FF),
            activeCardBorder = Color(0xFFD5E2FA),
            activeText = Color(0xFF2E6EDC),
            inactiveCardBg = Color(0xFFFFFFFF),
            inactiveCardBorder = Color(0xFFE6E7EB),
            inactiveText = Color(0xFF31353B),
            badgeActiveBg = Color(0xFFEEF4FF),
            badgeActiveText = Color(0xFF2E6EDC),
            badgeInactiveBg = Color(0xFFF8F8FA),
            badgeInactiveText = Color(0xFF8D929C),
            innerPanelBg = Color(0xFFF7F7F9),
            innerPanelBorder = Color(0xFFE9EAF0),
            buttonGreen = Color(0xFF2E6EDC),
            buttonSavedBg = Color(0xFFEEF4FF),
            buttonSavedText = Color(0xFF2E6EDC),
            circleActiveBg = Color(0xFF2E6EDC),
            circleActiveBorder = Color(0xFF2E6EDC),
            circleActiveCheck = Color(0xFFFFFFFF),
            circleInactiveBg = Color(0xFFFFFFFF),
            circleInactiveBorder = Color(0xFFD3D6DD),
            countBadgeBg = Color(0xFFF6F7F9),
            countBadgeBorder = Color(0xFFE7E8ED),
            countBadgeText = Color(0xFF5E636D),
            weightSavedBadgeBg = Color(0xFFEEF4FF),
            heatCellEmpty = Color(0xFFF3F6FC),
            heatCellLevel1 = Color(0xFFDDE9FB),
            heatCellLevel2 = Color(0xFFC1D8F8),
            heatCellLevel3 = Color(0xFF99BDF3),
            heatCellLevel4 = Color(0xFF6497EA),
            heatCellLevel5 = Color(0xFF2E6EDC),
            heatCellBorder = Color(0xFFD7E2F2),
            heatTodayBorder = Color(0xFF275CB7),
            navBarBg = Color(0xFFFFFFFF),
            dangerBg = Color(0xFFFFF5F5),
            dangerBorder = Color(0xFFF1DADB),
            dangerText = Color(0xFFB75B56)
        )

        AppThemeMode.White -> FitBoardPalette(
            bgGradientStart = Color(0xFFFEFFFF),
            bgGradientEnd = Color(0xFFF7F9FC),
            cardBg = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE3E8F0),
            textPrimary = Color(0xFF18212D),
            textSecondary = Color(0xFF667284),
            textHint = Color(0xFF8994A4),
            activeCardBg = Color(0xFFF1F6FF),
            activeCardBorder = Color(0xFFD8E4F8),
            activeText = Color(0xFF2A65C8),
            inactiveCardBg = Color(0xFFFFFFFF),
            inactiveCardBorder = Color(0xFFE3E8F0),
            inactiveText = Color(0xFF596779),
            badgeActiveBg = Color(0xFFEAF3FF),
            badgeActiveText = Color(0xFF2A65C8),
            badgeInactiveBg = Color(0xFFF5F7FA),
            badgeInactiveText = Color(0xFF8994A4),
            innerPanelBg = Color(0xFFF7F9FC),
            innerPanelBorder = Color(0xFFE6EBF3),
            buttonGreen = Color(0xFF2A65C8),
            buttonSavedBg = Color(0xFFEAF3FF),
            buttonSavedText = Color(0xFF2A65C8),
            circleActiveBg = Color(0xFF2A65C8),
            circleActiveBorder = Color(0xFF2A65C8),
            circleActiveCheck = Color(0xFFFFFFFF),
            circleInactiveBg = Color(0xFFF5F7FA),
            circleInactiveBorder = Color(0xFFD9E1EB),
            countBadgeBg = Color(0xFFF3F6FA),
            countBadgeBorder = Color(0xFFE2E8F0),
            countBadgeText = Color(0xFF687C90),
            weightSavedBadgeBg = Color(0xFFEAF3FF),
            heatCellEmpty = Color(0xFFF0F4FB),
            heatCellLevel1 = Color(0xFFDCE8FA),
            heatCellLevel2 = Color(0xFFBED5F6),
            heatCellLevel3 = Color(0xFF94BAEF),
            heatCellLevel4 = Color(0xFF6798E3),
            heatCellLevel5 = Color(0xFF2A65C8),
            heatCellBorder = Color(0xFFD8E2F1),
            heatTodayBorder = Color(0xFF2353A4),
            navBarBg = Color(0xFFFCFDFE),
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
            activeCardBg = Color(0xFF182133),
            activeCardBorder = Color(0xFF24344F),
            activeText = Color(0xFF8DB4FF),
            inactiveCardBg = Color(0xFF191919),
            inactiveCardBorder = Color(0xFF272727),
            inactiveText = Color(0xFFD0C7BD),
            badgeActiveBg = Color(0xFF1E2A40),
            badgeActiveText = Color(0xFF8DB4FF),
            badgeInactiveBg = Color(0xFF1F1F1F),
            badgeInactiveText = Color(0xFF96908A),
            innerPanelBg = Color(0xFF111111),
            innerPanelBorder = Color(0xFF232323),
            buttonGreen = Color(0xFF4D7BD9),
            buttonSavedBg = Color(0xFF1E2A40),
            buttonSavedText = Color(0xFF8DB4FF),
            circleActiveBg = Color(0xFF4D7BD9),
            circleActiveBorder = Color(0xFF4D7BD9),
            circleActiveCheck = Color(0xFFFFFFFF),
            circleInactiveBg = Color(0xFF202020),
            circleInactiveBorder = Color(0xFF303030),
            countBadgeBg = Color(0xFF1E1E1E),
            countBadgeBorder = Color(0xFF2C2C2C),
            countBadgeText = Color(0xFFB8AEA2),
            weightSavedBadgeBg = Color(0xFF1E1E1E),
            heatCellEmpty = Color(0xFF1B2130),
            heatCellLevel1 = Color(0xFF22304A),
            heatCellLevel2 = Color(0xFF294064),
            heatCellLevel3 = Color(0xFF3560A0),
            heatCellLevel4 = Color(0xFF4D7BD9),
            heatCellLevel5 = Color(0xFF8DB4FF),
            heatCellBorder = Color(0xFF233149),
            heatTodayBorder = Color(0xFF8DB4FF),
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
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, FitBoardColors.cardBorder, RoundedCornerShape(22.dp))
            .background(FitBoardColors.cardBg)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
internal fun CardLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = FitBoardColors.textHint
    )
}

@Composable
internal fun CardTitle(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = FitBoardColors.textPrimary
    )
}
