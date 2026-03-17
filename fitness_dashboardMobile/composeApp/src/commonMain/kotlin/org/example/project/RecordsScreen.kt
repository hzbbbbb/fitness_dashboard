package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecordsScreen(state: AppUiState, today: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // Header
        Column(Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = "今日记录",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
            Spacer(Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当日打卡详情",
                    fontSize = 14.sp,
                    color = FitBoardColors.textSecondary
                )
                Text(
                    text = today,
                    fontSize = 13.sp,
                    color = FitBoardColors.textHint
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Completeness banner
        RecordCompletenessBanner(state = state)
        Spacer(Modifier.height(12.dp))

        // Training record
        RecordSection(
            sectionLabel = "训练",
            sectionTitle = "今日训练",
            isEmpty = state.selectedTraining == null,
            emptyHint = "尚未选择训练类型"
        ) {
            RecordChip(
                label = state.selectedTraining ?: "",
                isActive = true
            )
        }
        Spacer(Modifier.height(12.dp))

        // Supplement record
        RecordSection(
            sectionLabel = "补剂",
            sectionTitle = "今日补剂",
            isEmpty = state.checkedSupplements.isEmpty(),
            emptyHint = "尚未记录补剂摄入"
        ) {
            state.supplementOptions.forEach { name ->
                val taken = name in state.checkedSupplements
                RecordSupplementRow(name = name, taken = taken)
            }
        }
        Spacer(Modifier.height(12.dp))

        // Weight record
        RecordSection(
            sectionLabel = "体重",
            sectionTitle = "空腹体重",
            isEmpty = state.savedWeight == null,
            emptyHint = "尚未记录体重"
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = state.savedWeight ?: "",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FitBoardColors.textPrimary
                )
                Text(
                    text = "kg",
                    fontSize = 14.sp,
                    color = FitBoardColors.textSecondary
                )
            }
        }

        // Note record (only shown when not empty)
        if (state.note.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            RecordSection(
                sectionLabel = "备注",
                sectionTitle = "今日备注",
                isEmpty = false,
                emptyHint = ""
            ) {
                Text(
                    text = state.note,
                    fontSize = 14.sp,
                    color = FitBoardColors.textPrimary,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}

// ─── Completeness Banner ───────────────────────────────────────────────────────

@Composable
private fun RecordCompletenessBanner(state: AppUiState) {
    val filledCount = listOfNotNull(
        state.selectedTraining,
        state.savedWeight,
        if (state.checkedSupplements.isNotEmpty()) "sup" else null,
        if (state.note.isNotEmpty()) "note" else null
    ).size
    val totalCount = 4
    val isComplete = filledCount == totalCount
    val pct = (filledCount * 100) / totalCount

    val bgColor = if (isComplete) FitBoardColors.activeCardBg else Color(0xFFF5F7F2)
    val borderColor = if (isComplete) FitBoardColors.activeCardBorder else FitBoardColors.cardBorder
    val dotColor = if (isComplete) FitBoardColors.badgeActiveText else FitBoardColors.badgeInactiveText

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = if (isComplete) "今日记录已完成" else "今日记录完成度",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = FitBoardColors.textPrimary
                )
            }
            Text(
                text = "$pct%",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isComplete) FitBoardColors.badgeActiveText else FitBoardColors.textSecondary
            )
        }
    }
}

// ─── Record Section Card ───────────────────────────────────────────────────────

@Composable
private fun RecordSection(
    sectionLabel: String,
    sectionTitle: String,
    isEmpty: Boolean,
    emptyHint: String,
    content: @Composable () -> Unit
) {
    FitCard {
        CardLabel(sectionLabel)
        Spacer(Modifier.height(2.dp))
        CardTitle(sectionTitle)
        Spacer(Modifier.height(12.dp))

        if (isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        FitBoardColors.inactiveCardBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .background(FitBoardColors.inactiveCardBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = emptyHint,
                    fontSize = 13.sp,
                    color = FitBoardColors.textHint
                )
            }
        } else {
            content()
        }
    }
}

// ─── Record Chip (training type badge) ────────────────────────────────────────

@Composable
private fun RecordChip(label: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                if (isActive) FitBoardColors.activeCardBorder else FitBoardColors.inactiveCardBorder,
                RoundedCornerShape(20.dp)
            )
            .background(if (isActive) FitBoardColors.activeCardBg else FitBoardColors.inactiveCardBg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) FitBoardColors.activeText else FitBoardColors.inactiveText
        )
    }
}

// ─── Supplement Row in Records ────────────────────────────────────────────────

@Composable
private fun RecordSupplementRow(name: String, taken: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (taken) FitBoardColors.activeCardBorder else FitBoardColors.inactiveCardBorder,
                RoundedCornerShape(12.dp)
            )
            .background(if (taken) FitBoardColors.activeCardBg else FitBoardColors.inactiveCardBg)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (taken) FitBoardColors.activeText else FitBoardColors.inactiveText
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (taken) FitBoardColors.badgeActiveBg else FitBoardColors.badgeInactiveBg)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (taken) "已吃" else "未吃",
                fontSize = 11.sp,
                color = if (taken) FitBoardColors.badgeActiveText else FitBoardColors.badgeInactiveText
            )
        }
    }
    Spacer(Modifier.height(6.dp))
}
