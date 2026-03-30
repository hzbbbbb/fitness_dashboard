package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    today: String,
    onStateChange: (AppUiState) -> Unit
) {
    var currentPage by remember { mutableStateOf(HomePage.Summary) }

    when (currentPage) {
        HomePage.Summary -> HomeSummaryPage(
            state = state,
            dateInfo = dateInfo,
            today = today,
            onEditClick = { currentPage = HomePage.Editor }
        )

        HomePage.Editor -> HomeSummaryEditorPage(
            selectedCards = state.homeVisibleCards,
            onBack = { currentPage = HomePage.Summary },
            onAddCard = { card ->
                onStateChange(state.copy(homeVisibleCards = state.homeVisibleCards + card))
            },
            onRemoveCard = { card ->
                onStateChange(state.copy(homeVisibleCards = state.homeVisibleCards - card))
            }
        )
    }
}

private enum class HomePage {
    Summary,
    Editor
}

@Composable
private fun HomeSummaryPage(
    state: AppUiState,
    dateInfo: DateInfo,
    today: String,
    onEditClick: () -> Unit
) {
    val healthState = currentHealthSummaryState()
    val scoreState = buildHealthScoreState(state = state, healthState = healthState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        HeatmapCard(
            dateInfo = dateInfo,
            hasRecordToday = state.hasAnyRecord(),
            today = today
        )

        Spacer(Modifier.height(16.dp))

        HomeSectionHeader(
            title = "摘要",
            subtitle = "参考系统健康摘要方式，可直接在这里编辑卡片",
            trailingAction = "编辑",
            onTrailingClick = onEditClick
        )

        Spacer(Modifier.height(10.dp))

        val visibleCards = state.homeCardsInDisplayOrder()
        if (visibleCards.isEmpty()) {
            HomeCardsEmptyState()
        } else {
            visibleCards.forEachIndexed { index, card ->
                HomeSummaryCardBlock(
                    card = card,
                    state = state,
                    healthState = healthState,
                    scoreState = scoreState
                )
                if (index != visibleCards.lastIndex) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String,
    trailingAction: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = FitBoardColors.textSecondary
            )
        }
        if (!trailingAction.isNullOrBlank() && onTrailingClick != null) {
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, FitBoardColors.countBadgeBorder, RoundedCornerShape(18.dp))
                    .background(FitBoardColors.cardBg)
                    .clickable { onTrailingClick() }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = trailingAction,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FitBoardColors.textPrimary
                )
            }
        }
    }
}

@Composable
internal fun HeatmapCard(
    dateInfo: DateInfo,
    hasRecordToday: Boolean,
    today: String
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
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "FitBoard",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FitBoardColors.textPrimary
                )
                Text(
                    text = "最近一个月记录热力图",
                    fontSize = 14.sp,
                    color = FitBoardColors.textSecondary
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, FitBoardColors.countBadgeBorder, RoundedCornerShape(18.dp))
                        .background(FitBoardColors.countBadgeBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${dateInfo.year}年${monthLabels[dateInfo.month - 1]}",
                        fontSize = 12.sp,
                        color = FitBoardColors.countBadgeText
                    )
                }
                Text(
                    text = today,
                    fontSize = 12.sp,
                    color = FitBoardColors.textHint
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(16.dp))
                .background(FitBoardColors.innerPanelBg)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column {
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

                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0..4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeatmapLegendPill(
                color = FitBoardColors.heatCellTodayRecord,
                text = "已记录"
            )
            HeatmapLegendPill(
                color = FitBoardColors.heatCellEmpty,
                text = "无记录"
            )
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
    if (dayOffset > 0 || dayOffset < -29) {
        Spacer(modifier = modifier.aspectRatio(1f))
        return
    }

    val fillColor = when {
        isToday && hasRecord -> FitBoardColors.heatCellTodayRecord
        isToday -> FitBoardColors.heatCellToday
        hasRecord -> FitBoardColors.heatCellRecord
        else -> FitBoardColors.heatCellEmpty
    }
    val borderColor = when {
        isToday && hasRecord -> FitBoardColors.heatTodayBorder
        isToday -> FitBoardColors.heatTodayBorder
        hasRecord -> FitBoardColors.heatTodayBorder.copy(alpha = 0.72f)
        else -> FitBoardColors.cardBorder
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(FitBoardColors.cardBg)
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .background(fillColor)
        )
    }
}

@Composable
private fun HeatmapLegendPill(
    color: Color,
    text: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.innerPanelBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Text(
            text = text,
            fontSize = 10.sp,
            color = FitBoardColors.textHint
        )
    }
}

@Composable
private fun HomeSummaryCardBlock(
    card: HomeSummaryCard,
    state: AppUiState,
    healthState: HealthSummaryUiState,
    scoreState: HealthScorePageState
) {
    when (card) {
        HomeSummaryCard.Steps -> HomeMetricCard(
            label = "步数",
            title = "今日步数",
            value = if (healthState.hasTodaySteps) "${healthState.todaySteps}" else "暂无",
            unit = if (healthState.hasTodaySteps) "步" else "",
            summary = when {
                healthState.hasTodaySteps -> "目标 ${state.stepGoal} 步"
                else -> healthState.statusMessage
            },
            footnote = healthState.lastUpdatedAt.takeIf { it.isNotBlank() }?.let { "更新于 $it" }
        )

        HomeSummaryCard.Sleep -> HomeMetricCard(
            label = "睡眠",
            title = "昨晚睡眠",
            value = if (healthState.hasSleepDuration) formatSleepDuration(healthState.sleepDurationHours) else "暂无",
            unit = "",
            summary = if (healthState.hasSleepDuration) {
                "目标 ${formatSleepGoalSummary(state.sleepGoalHours, state.sleepGoalMinutes)}"
            } else {
                healthState.statusMessage
            },
            footnote = healthState.lastUpdatedAt.takeIf { it.isNotBlank() }?.let { "更新于 $it" }
        )

        HomeSummaryCard.Score -> HomeScoreCard(scoreState = scoreState)

        HomeSummaryCard.Supplements -> HomeStatusCard(
            label = "补剂",
            title = "补剂摄入情况",
            primary = if (state.checkedSupplements.isEmpty()) {
                "今日未记录"
            } else {
                "${state.checkedSupplements.size}/${state.supplementOptions.size}"
            },
            secondary = when {
                state.checkedSupplements.isEmpty() -> "还没有保存补剂记录"
                else -> state.checkedSupplements.joinToString("、")
            }
        )

        HomeSummaryCard.Training -> HomeStatusCard(
            label = "训练",
            title = "训练情况",
            primary = state.selectedTraining ?: "今日未记录",
            secondary = if (state.selectedTraining == null) {
                "还没有保存训练记录"
            } else {
                "训练已纳入今日摘要与健康分"
            }
        )
    }
}

@Composable
private fun HomeMetricCard(
    label: String,
    title: String,
    value: String,
    unit: String,
    summary: String,
    footnote: String?
) {
    FitCard {
        CardLabel(label)
        Spacer(Modifier.height(2.dp))
        CardTitle(title)
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FitBoardColors.textPrimary
                )
                if (unit.isNotBlank()) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = unit,
                        fontSize = 14.sp,
                        color = FitBoardColors.textSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = summary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = FitBoardColors.textSecondary
        )

        if (!footnote.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = footnote,
                fontSize = 11.sp,
                color = FitBoardColors.textHint
            )
        }
    }
}

@Composable
private fun HomeScoreCard(scoreState: HealthScorePageState) {
    FitCard {
        CardLabel("评分")
        Spacer(Modifier.height(2.dp))
        CardTitle("今日健康分")
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = formatScore(scoreState.totalScore),
                fontSize = 38.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
            ScoreTag(text = scoreState.levelLabel)
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "睡眠、步数、训练和补剂共同构成当天评分。",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = FitBoardColors.textSecondary
        )
    }
}

@Composable
private fun HomeStatusCard(
    label: String,
    title: String,
    primary: String,
    secondary: String
) {
    FitCard {
        CardLabel(label)
        Spacer(Modifier.height(2.dp))
        CardTitle(title)
        Spacer(Modifier.height(14.dp))

        Text(
            text = primary,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = FitBoardColors.textPrimary
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = secondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = FitBoardColors.textSecondary
        )
    }
}

@Composable
private fun HomeCardsEmptyState() {
    FitCard {
        CardLabel("摘要")
        Spacer(Modifier.height(2.dp))
        CardTitle("首页当前没有显示卡片")
        Spacer(Modifier.height(10.dp))
        Text(
            text = "你可以前往设置里的“首页内容设置”，选择要显示的摘要卡片。",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = FitBoardColors.textSecondary
        )
    }
}

@Composable
private fun HomeSummaryEditorPage(
    selectedCards: Set<HomeSummaryCard>,
    onBack: () -> Unit,
    onAddCard: (HomeSummaryCard) -> Unit,
    onRemoveCard: (HomeSummaryCard) -> Unit
) {
    val visibleCards = HomeSummaryCard.entries.filter { it in selectedCards }
    val hiddenCards = HomeSummaryCard.entries.filter { it !in selectedCards }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        HomeEditorTopBar(onBack = onBack)
        Spacer(Modifier.height(16.dp))

        HomeSectionHeader(
            title = "编辑摘要",
            subtitle = "热力图会始终固定在首页顶部"
        )

        Spacer(Modifier.height(12.dp))

        FitCard {
            CardLabel("已显示")
            Spacer(Modifier.height(2.dp))
            CardTitle("当前摘要卡片")
            Spacer(Modifier.height(12.dp))

            if (visibleCards.isEmpty()) {
                EmptyEditorState(text = "当前没有显示中的摘要卡片。")
            } else {
                visibleCards.forEachIndexed { index, card ->
                    EditorCardRow(
                        title = card.title,
                        description = card.description,
                        actionLabel = "移除",
                        onAction = { onRemoveCard(card) }
                    )
                    if (index != visibleCards.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        FitCard {
            CardLabel("可添加")
            Spacer(Modifier.height(2.dp))
            CardTitle("更多摘要卡片")
            Spacer(Modifier.height(12.dp))

            if (hiddenCards.isEmpty()) {
                EmptyEditorState(text = "当前 5 个候选项都已经显示。")
            } else {
                hiddenCards.forEachIndexed { index, card ->
                    EditorCardRow(
                        title = card.title,
                        description = card.description,
                        actionLabel = "添加",
                        onAction = { onAddCard(card) }
                    )
                    if (index != hiddenCards.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun HomeEditorTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.countBadgeBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.cardBg)
            .clickable { onBack() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = "返回摘要",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = FitBoardColors.textPrimary
        )
    }
}

@Composable
private fun EditorCardRow(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(14.dp))
            .background(FitBoardColors.innerPanelBg)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = FitBoardColors.textPrimary
            )
            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = FitBoardColors.textHint
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, FitBoardColors.countBadgeBorder, RoundedCornerShape(14.dp))
                .background(FitBoardColors.cardBg)
                .clickable { onAction() }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FitBoardColors.textPrimary
            )
        }
    }
}

@Composable
private fun EmptyEditorState(text: String) {
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
            lineHeight = 19.sp,
            color = FitBoardColors.textSecondary
        )
    }
}

private fun formatSleepGoalSummary(hour: Int, minute: Int): String {
    return if (minute == 0) {
        "${hour}小时"
    } else {
        "${hour}小时${minute}分"
    }
}
