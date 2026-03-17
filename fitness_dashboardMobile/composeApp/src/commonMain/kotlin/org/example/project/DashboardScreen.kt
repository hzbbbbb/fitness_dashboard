package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Screen ────────────────────────────────────────────────────────────────────
// Background gradient is applied by MainScaffold; this screen is transparent.
// statusBarsPadding() handles the iOS status bar (notch / Dynamic Island).

@Composable
fun DashboardScreen(
    state: AppUiState,
    dateInfo: DateInfo,
    today: String,
    onStateChange: (AppUiState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        DashboardHeader(today = today)
        Spacer(Modifier.height(16.dp))

        HeatmapCard(dateInfo = dateInfo, hasRecordToday = state.hasAnyRecord())
        Spacer(Modifier.height(12.dp))

        TodaySummaryCard(state = state)
        Spacer(Modifier.height(12.dp))

        WeightCard(
            weightInput = state.weightInput,
            savedWeight = state.savedWeight,
            onWeightChange = { onStateChange(state.copy(weightInput = it, isSaved = false)) },
            onSave = {
                val trimmed = state.weightInput.trim()
                if (trimmed.isNotEmpty()) onStateChange(state.copy(savedWeight = trimmed))
            }
        )
        Spacer(Modifier.height(12.dp))

        TrainingCard(
            selected = state.selectedTraining,
            options = state.trainingOptions,
            onSelect = { onStateChange(state.copy(selectedTraining = it, isSaved = false)) }
        )
        Spacer(Modifier.height(12.dp))

        SupplementCard(
            checked = state.checkedSupplements,
            options = state.supplementOptions,
            onToggle = { key ->
                val updated = if (key in state.checkedSupplements)
                    state.checkedSupplements - key
                else
                    state.checkedSupplements + key
                onStateChange(state.copy(checkedSupplements = updated, isSaved = false))
            }
        )
        Spacer(Modifier.height(12.dp))

        NoteCard(
            note = state.note,
            onNoteChange = { onStateChange(state.copy(note = it, isSaved = false)) }
        )
        Spacer(Modifier.height(20.dp))

        SaveButton(
            isSaved = state.isSaved,
            onClick = { onStateChange(state.copy(isSaved = true)) }
        )
        Spacer(Modifier.height(28.dp))
    }
}

// ─── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(today: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = "FitBoard",
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            color = FitBoardColors.textPrimary,
        )
        Spacer(Modifier.height(3.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "今日概览",
                fontSize = 14.sp,
                color = FitBoardColors.textSecondary,
            )
            Text(
                text = today,
                fontSize = 13.sp,
                color = FitBoardColors.textHint,
            )
        }
    }
}

// ─── Heatmap Card ──────────────────────────────────────────────────────────────
// Shows a 5×7 grid (35 cells) where today is always in the last row at the
// column corresponding to its day-of-week. The grid scrolls back ~30 days.
//
// Grid mapping:
//   todayIndex = 4*7 + (dayOfWeek-1)   → last row, correct column
//   dayOffset  = cellIndex - todayIndex → negative=past, 0=today, positive=future

@Composable
private fun HeatmapCard(
    dateInfo: DateInfo,
    hasRecordToday: Boolean
) {
    val monthLabels = listOf(
        "1月","2月","3月","4月","5月","6月",
        "7月","8月","9月","10月","11月","12月"
    )
    val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")
    // dayOfWeek: 1=Mon … 7=Sun → column index: 0–6
    val todayCol   = dateInfo.dayOfWeek - 1
    val todayIndex = 4 * 7 + todayCol  // 0-based cell index in 5×7 grid

    FitCard {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                CardLabel("活动记录")
                Spacer(Modifier.height(2.dp))
                CardTitle("最近一个月")
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, FitBoardColors.countBadgeBorder, RoundedCornerShape(20.dp))
                    .background(FitBoardColors.countBadgeBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${dateInfo.year}年${monthLabels[dateInfo.month - 1]}",
                    fontSize = 12.sp,
                    color = FitBoardColors.countBadgeText
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Weekday column headers
        Row(modifier = Modifier.fillMaxWidth()) {
            weekLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = FitBoardColors.textHint
                    )
                }
            }
        }

        Spacer(Modifier.height(5.dp))

        // 5-row × 7-column grid
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0..4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayOffset = cellIndex - todayIndex
                        HeatmapCell(
                            modifier = Modifier.weight(1f),
                            dayOffset = dayOffset,
                            hasRecord = dayOffset == 0 && hasRecordToday,
                            isToday = dayOffset == 0
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Legend
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(FitBoardColors.heatCellTodayRecord)
            Text("有记录", fontSize = 10.sp, color = FitBoardColors.textHint)
            LegendDot(FitBoardColors.heatCellEmpty)
            Text("无记录", fontSize = 10.sp, color = FitBoardColors.textHint)
        }
    }
}

@Composable
private fun HeatmapCell(
    modifier: Modifier,
    dayOffset: Int,
    hasRecord: Boolean,
    isToday: Boolean
) {
    val bgColor = when {
        dayOffset > 0          -> Color.Transparent          // future — invisible
        dayOffset < -29        -> Color.Transparent          // > 30 days ago — invisible
        isToday && hasRecord   -> FitBoardColors.heatCellTodayRecord
        isToday                -> FitBoardColors.heatCellToday
        hasRecord              -> FitBoardColors.heatCellRecord
        else                   -> FitBoardColors.heatCellEmpty
    }
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .then(
                if (isToday && !hasRecord)
                    Modifier.border(1.5.dp, FitBoardColors.heatTodayBorder, shape)
                else Modifier
            )
            .background(bgColor)
    )
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
    )
}

// ─── Today Summary Card ────────────────────────────────────────────────────────

@Composable
private fun TodaySummaryCard(state: AppUiState) {
    FitCard {
        CardLabel("今日")
        Spacer(Modifier.height(2.dp))
        CardTitle("今日概况")
        Spacer(Modifier.height(12.dp))

        SummaryRow(
            label = "训练类型",
            value = state.selectedTraining ?: "暂未选择",
            hasValue = state.selectedTraining != null
        )
        Spacer(Modifier.height(8.dp))

        val supplementText = when {
            state.checkedSupplements.isEmpty() -> "暂未记录"
            else -> {
                val names = state.checkedSupplements.joinToString("、")
                "${state.checkedSupplements.size}/${state.supplementOptions.size} · $names"
            }
        }
        SummaryRow(
            label = "今日补剂",
            value = supplementText,
            hasValue = state.checkedSupplements.isNotEmpty()
        )
        Spacer(Modifier.height(8.dp))

        SummaryRow(
            label = "空腹体重",
            value = state.savedWeight?.let { "$it kg" } ?: "暂未记录",
            hasValue = state.savedWeight != null
        )

        if (state.note.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            SummaryRow(
                label = "备注",
                value = if (state.note.length > 30) state.note.take(30) + "…" else state.note,
                hasValue = true
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, hasValue: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = FitBoardColors.textSecondary,
            modifier = Modifier.width(64.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            color = if (hasValue) FitBoardColors.textPrimary else FitBoardColors.textHint,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

// ─── Weight Card ───────────────────────────────────────────────────────────────

@Composable
private fun WeightCard(
    weightInput: String,
    savedWeight: String?,
    onWeightChange: (String) -> Unit,
    onSave: () -> Unit
) {
    FitCard {
        CardLabel("体重")
        Spacer(Modifier.height(2.dp))
        CardTitle("空腹体重")
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = weightInput,
                onValueChange = onWeightChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("输入体重 (kg)", color = FitBoardColors.textHint, fontSize = 14.sp)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFBED9C1),
                    unfocusedBorderColor = FitBoardColors.inactiveCardBorder,
                    focusedContainerColor = Color(0xFFFBFCF8),
                    unfocusedContainerColor = FitBoardColors.inactiveCardBg,
                    cursorColor = FitBoardColors.textPrimary,
                    focusedTextColor = FitBoardColors.textPrimary,
                    unfocusedTextColor = FitBoardColors.textPrimary,
                )
            )
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitBoardColors.buttonGreen,
                    contentColor = Color.White,
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text("保存", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (savedWeight != null) {
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("已记录：", fontSize = 13.sp, color = FitBoardColors.textSecondary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(FitBoardColors.weightSavedBadgeBg)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$savedWeight kg",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = FitBoardColors.badgeActiveText
                    )
                }
            }
        }
    }
}

// ─── Training Card ─────────────────────────────────────────────────────────────

@Composable
private fun TrainingCard(
    selected: String?,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    FitCard {
        CardLabel("训练")
        Spacer(Modifier.height(2.dp))
        CardTitle("今日训练")
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(16.dp))
                .background(FitBoardColors.innerPanelBg)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { option ->
                SelectableItem(
                    label = option,
                    isActive = option == selected,
                    activeLabel = "已选",
                    inactiveLabel = "未选",
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

// ─── Supplement Card ───────────────────────────────────────────────────────────

@Composable
private fun SupplementCard(
    checked: Set<String>,
    options: List<String>,
    onToggle: (String) -> Unit
) {
    FitCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                CardLabel("补剂")
                Spacer(Modifier.height(2.dp))
                CardTitle("今日补剂")
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, FitBoardColors.countBadgeBorder, RoundedCornerShape(20.dp))
                    .background(FitBoardColors.countBadgeBg)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${checked.size}/${options.size}",
                    fontSize = 13.sp,
                    color = FitBoardColors.countBadgeText
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(16.dp))
                .background(FitBoardColors.innerPanelBg)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { key ->
                SelectableItem(
                    label = key,
                    isActive = key in checked,
                    activeLabel = "已吃",
                    inactiveLabel = "未吃",
                    onClick = { onToggle(key) }
                )
            }
        }
    }
}

// ─── Shared Selectable Item ────────────────────────────────────────────────────

@Composable
private fun SelectableItem(
    label: String,
    isActive: Boolean,
    activeLabel: String,
    inactiveLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isActive) FitBoardColors.activeCardBorder else FitBoardColors.inactiveCardBorder,
                RoundedCornerShape(12.dp)
            )
            .background(if (isActive) FitBoardColors.activeCardBg else FitBoardColors.inactiveCardBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(
                        1.dp,
                        if (isActive) FitBoardColors.circleActiveBorder else FitBoardColors.circleInactiveBorder,
                        CircleShape
                    )
                    .background(if (isActive) FitBoardColors.circleActiveBg else FitBoardColors.circleInactiveBg),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    Text("✓", fontSize = 10.sp, color = FitBoardColors.circleActiveCheck)
                }
            }
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isActive) FitBoardColors.activeText else FitBoardColors.inactiveText
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isActive) FitBoardColors.badgeActiveBg else FitBoardColors.badgeInactiveBg)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (isActive) activeLabel else inactiveLabel,
                fontSize = 11.sp,
                color = if (isActive) FitBoardColors.badgeActiveText else FitBoardColors.badgeInactiveText
            )
        }
    }
}

// ─── Note Card ─────────────────────────────────────────────────────────────────

@Composable
private fun NoteCard(note: String, onNoteChange: (String) -> Unit) {
    FitCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardTitle("备注")
            Text(
                text = "${note.length}/140",
                fontSize = 11.sp,
                color = FitBoardColors.textHint
            )
        }
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { if (it.length <= 140) onNoteChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 96.dp),
            placeholder = {
                Text(
                    "记录今日状态、训练强度或恢复感受...",
                    color = FitBoardColors.textHint,
                    fontSize = 13.sp
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFBED9C1),
                unfocusedBorderColor = FitBoardColors.inactiveCardBorder,
                focusedContainerColor = Color(0xFFFBFCF8),
                unfocusedContainerColor = FitBoardColors.inactiveCardBg,
                cursorColor = FitBoardColors.textPrimary,
                focusedTextColor = FitBoardColors.textPrimary,
                unfocusedTextColor = FitBoardColors.textPrimary,
            ),
            maxLines = 5,
        )
    }
}

// ─── Save Button ───────────────────────────────────────────────────────────────

@Composable
private fun SaveButton(isSaved: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSaved) FitBoardColors.buttonSavedBg else FitBoardColors.buttonGreen,
            contentColor = if (isSaved) FitBoardColors.buttonSavedText else Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = if (isSaved) "✓  已保存今日记录" else "保存今日记录",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
