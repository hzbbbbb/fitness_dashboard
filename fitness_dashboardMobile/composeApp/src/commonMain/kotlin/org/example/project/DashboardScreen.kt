package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    state: AppUiState,
    dateInfo: DateInfo,
    today: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        HomeHeader(today = today)
        Spacer(Modifier.height(16.dp))

        HeatmapCard(dateInfo = dateInfo, hasRecordToday = state.hasAnyRecord())
        Spacer(Modifier.height(12.dp))

        TodaySummaryCard(state = state)
        Spacer(Modifier.height(12.dp))

        HealthSummaryCard()
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun HomeHeader(today: String) {
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
                text = "健康概览",
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

@Composable
internal fun HeatmapCard(
    dateInfo: DateInfo,
    hasRecordToday: Boolean
) {
    val monthLabels = listOf(
        "1月", "2月", "3月", "4月", "5月", "6月",
        "7月", "8月", "9月", "10月", "11月", "12月"
    )
    val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")
    val todayCol = dateInfo.dayOfWeek - 1
    val todayIndex = 4 * 7 + todayCol

    FitCard {
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
        dayOffset > 0 -> Color.Transparent
        dayOffset < -29 -> Color.Transparent
        isToday && hasRecord -> FitBoardColors.heatCellTodayRecord
        isToday -> FitBoardColors.heatCellToday
        hasRecord -> FitBoardColors.heatCellRecord
        else -> FitBoardColors.heatCellEmpty
    }
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .then(
                if (isToday && !hasRecord) {
                    Modifier.border(1.5.dp, FitBoardColors.heatTodayBorder, shape)
                } else {
                    Modifier
                }
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

@Composable
internal fun TodaySummaryCard(state: AppUiState) {
    FitCard {
        CardLabel("今日")
        Spacer(Modifier.height(2.dp))
        CardTitle("今日概况")
        Spacer(Modifier.height(12.dp))

        SummaryRow(
            label = "训练状态",
            value = state.selectedTraining?.let { "已选择 · $it" } ?: "未记录训练",
            hasValue = state.selectedTraining != null
        )
        Spacer(Modifier.height(8.dp))

        val supplementText = when {
            state.checkedSupplements.isEmpty() -> "未记录补剂"
            else -> {
                val names = state.checkedSupplements.joinToString("、")
                "已吃 ${state.checkedSupplements.size}/${state.supplementOptions.size} · $names"
            }
        }
        SummaryRow(
            label = "补剂摄入",
            value = supplementText,
            hasValue = state.checkedSupplements.isNotEmpty()
        )
        Spacer(Modifier.height(8.dp))

        SummaryRow(
            label = "体重摘要",
            value = state.savedWeight?.let { "$it kg" } ?: "未记录体重",
            hasValue = state.savedWeight != null
        )
        Spacer(Modifier.height(8.dp))

        SummaryRow(
            label = "备注摘要",
            value = state.note.takeIf { it.isNotBlank() }?.let {
                if (it.length > 34) it.take(34) + "…" else it
            } ?: "暂无备注",
            hasValue = state.note.isNotBlank()
        )
    }
}

@Composable
private fun HealthSummaryCard() {
    val healthState = currentHealthSummaryState()

    FitCard {
        CardLabel("健康")
        Spacer(Modifier.height(2.dp))
        CardTitle("Apple 健康摘要")
        Spacer(Modifier.height(12.dp))

        when (healthState.authorizationState) {
            HealthAuthorizationState.Loading -> {
                HealthEmptyState(text = healthState.statusMessage)
            }

            HealthAuthorizationState.Denied,
            HealthAuthorizationState.Unavailable,
            HealthAuthorizationState.Error,
            HealthAuthorizationState.Idle -> {
                HealthEmptyState(text = healthState.statusMessage)
            }

            HealthAuthorizationState.Authorized -> {
                SummaryRow(
                    label = "今日步数",
                    value = if (healthState.hasTodaySteps) {
                        "${healthState.todaySteps} 步"
                    } else {
                        "暂无步数数据"
                    },
                    hasValue = healthState.hasTodaySteps
                )
                Spacer(Modifier.height(8.dp))

                SummaryRow(
                    label = "睡眠时长",
                    value = if (healthState.hasSleepDuration) {
                        formatSleepDuration(healthState.sleepDurationHours)
                    } else {
                        "暂无睡眠数据"
                    },
                    hasValue = healthState.hasSleepDuration
                )

                if (healthState.statusMessage.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = healthState.statusMessage,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = FitBoardColors.textSecondary
                    )
                }

                if (healthState.lastUpdatedAt.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "更新于 ${healthState.lastUpdatedAt}",
                        fontSize = 11.sp,
                        color = FitBoardColors.textHint,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthEmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(14.dp))
            .background(FitBoardColors.innerPanelBg)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = FitBoardColors.textSecondary
        )
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
