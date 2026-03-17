package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Design Tokens ─────────────────────────────────────────────────────────────
// Visible to all files in this package (internal).
// Single source of truth for the FitBoard design language.

internal object FitBoardColors {
    // Page background
    val bgGradientStart     = Color(0xFFF7F4EA)
    val bgGradientEnd       = Color(0xFFEEF5EE)

    // Card
    val cardBg              = Color(0xFFF9FBF6)
    val cardBorder          = Color(0xFFDCE4D5)

    // Text
    val textPrimary         = Color(0xFF243127)
    val textSecondary       = Color(0xFF7D897A)
    val textHint            = Color(0xFF8C978A)

    // Selectable item – active state
    val activeCardBg        = Color(0xFFEDF7EF)
    val activeCardBorder    = Color(0xFFCBE0CD)
    val activeText          = Color(0xFF25472D)

    // Selectable item – inactive state
    val inactiveCardBg      = Color(0xFFFFFFFF)
    val inactiveCardBorder  = Color(0xFFDDE4D9)
    val inactiveText        = Color(0xFF5F6D5F)

    // Badges
    val badgeActiveBg       = Color(0xFFDFF0E2)
    val badgeActiveText     = Color(0xFF3E7248)
    val badgeInactiveBg     = Color(0xFFF1F3EE)
    val badgeInactiveText   = Color(0xFF879185)

    // Inner panel (list container inside a card)
    val innerPanelBg        = Color(0xFFF6FAF3)
    val innerPanelBorder    = Color(0xFFE3EADF)

    // Buttons
    val buttonGreen         = Color(0xFF4A7A56)
    val buttonSavedBg       = Color(0xFFDFF0E2)
    val buttonSavedText     = Color(0xFF3E7248)

    // Circle check icon
    val circleActiveBg      = Color(0xFFDFF0E2)
    val circleActiveBorder  = Color(0xFFB9D7BC)
    val circleActiveCheck   = Color(0xFF387147)
    val circleInactiveBg    = Color(0xFFF7F8F3)
    val circleInactiveBorder = Color(0xFFD5DDD2)

    // Count badge
    val countBadgeBg        = Color(0xFFEEF6EC)
    val countBadgeBorder    = Color(0xFFD8E5D8)
    val countBadgeText      = Color(0xFF52725A)

    // Weight saved badge
    val weightSavedBadgeBg  = Color(0xFFEEF6EC)

    // Heatmap
    val heatCellEmpty        = Color(0xFFE8EEE4)  // past, no record
    val heatCellToday        = Color(0xFFBED9C1)  // today, no record yet
    val heatCellTodayRecord  = Color(0xFF6AAA7A)  // today, has record
    val heatCellRecord       = Color(0xFF8EC99B)  // past, has record (future data use)
    val heatTodayBorder      = Color(0xFF8BC39A)  // today border ring

    // Navigation bar
    val navBarBg            = Color(0xFFF0F4EC)

    // Delete / danger
    val dangerBg            = Color(0xFFFFF1F0)
    val dangerBorder        = Color(0xFFFFCDD0)
    val dangerText          = Color(0xFFD9534F)
}

// ─── Base Card Shell ───────────────────────────────────────────────────────────

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

// ─── Typography Helpers ────────────────────────────────────────────────────────

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
