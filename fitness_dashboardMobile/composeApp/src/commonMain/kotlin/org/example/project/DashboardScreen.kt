package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private const val HEATMAP_WEEKS = 13

private val HeatmapCardBorderColor = Color(0xFFE1E6EE)
private val HeatmapPanelColor = Color(0xFFF4F7FB)
private val HeatmapPanelBorderColor = Color(0xFFE4EAF2)
private val HeatmapEmptyCellColor = Color(0xFFE3E9F2)
private val HeatmapLevel1Color = Color(0xFFD5E2F8)
private val HeatmapLevel2Color = Color(0xFFB7D0F6)
private val HeatmapLevel3Color = Color(0xFF8EB4F0)
private val HeatmapLevel4Color = Color(0xFF5F8FE0)
private val HeatmapLevel5Color = Color(0xFF275FCA)

private data class HeatmapModel(
    val weeks: List<HeatmapWeekColumn>
)

private data class HeatmapWeekColumn(
    val days: List<HeatmapDayCell>
)

private data class HeatmapDayCell(
    val date: CalendarDay,
    val score: Int?,
    val level: HeatmapScoreLevel,
    val isToday: Boolean,
    val isFuture: Boolean
)

private data class HeatmapLayoutMetrics(
    val cellSize: Dp,
    val cellGap: Dp,
    val weekGap: Dp
)

private enum class HeatmapScoreLevel {
    Empty,
    Level1,
    Level2,
    Level3,
    Level4,
    Level5
}

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
    val visibleCards = state.homeCardsInDisplayOrder()
    val todayDate = remember(dateInfo) { dateInfo.toCalendarDay() }
    val historicalKeys = remember(todayDate) {
        buildHeatmapWeekDates(todayDate)
            .flatten()
            .map(CalendarDay::toStorageKey)
            .filter { it != todayDate.toStorageKey() }
    }
    val historicalRecords = remember(historicalKeys) {
        FitBoardFileStore.loadHistoricalDailyRecords(historicalKeys)
    }
    val heatmapModel = remember(
        todayDate,
        historicalRecords,
        state.sleepGoalHours,
        state.sleepGoalMinutes,
        state.stepGoal,
        state.supplementOptions,
        state.selectedTraining,
        state.checkedSupplements,
        healthState.authorizationState,
        healthState.todaySteps,
        healthState.hasTodaySteps,
        healthState.sleepDurationHours,
        healthState.hasSleepDuration
    ) {
        buildHeatmapModel(
            today = todayDate,
            currentState = state,
            currentHealthState = healthState,
            historicalRecords = historicalRecords
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        HeatmapCard(
            heatmapModel = heatmapModel
        )

        Spacer(Modifier.height(20.dp))

        HomeSectionHeader(
            title = "摘要",
            trailingAction = "编辑",
            onTrailingClick = onEditClick
        )

        Spacer(Modifier.height(12.dp))

        if (visibleCards.isEmpty()) {
            HomeCardsEmptyState(onEditClick = onEditClick)
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
    subtitle: String? = null,
    trailingAction: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = FitBoardColors.textSecondary
                )
            }
        }

        if (!trailingAction.isNullOrBlank() && onTrailingClick != null) {
            Spacer(Modifier.width(14.dp))
            QuietActionButton(
                label = trailingAction,
                onClick = onTrailingClick
            )
        }
    }
}

@Composable
private fun HeatmapCard(
    heatmapModel: HeatmapModel
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, HeatmapCardBorderColor, RoundedCornerShape(24.dp))
            .background(FitBoardColors.cardBg)
    ) {
        val metrics = rememberHeatmapLayoutMetrics(maxWidth = maxWidth)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = "FitBoard",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, HeatmapPanelBorderColor, RoundedCornerShape(20.dp))
                    .background(HeatmapPanelColor)
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = metrics.weekGap,
                            alignment = Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.Top
                    ) {
                        heatmapModel.weeks.forEach { week ->
                            HeatmapWeekGrid(
                                week = week,
                                metrics = metrics
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapWeekGrid(
    week: HeatmapWeekColumn,
    metrics: HeatmapLayoutMetrics
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(metrics.cellGap),
        modifier = Modifier.width(metrics.cellSize)
    ) {
        week.days.forEach { cell ->
            HeatmapCell(
                cell = cell,
                metrics = metrics
            )
        }
    }
}

@Composable
private fun HeatmapCell(
    cell: HeatmapDayCell,
    metrics: HeatmapLayoutMetrics
) {
    if (cell.isFuture) {
        Spacer(
            modifier = Modifier
                .size(metrics.cellSize)
                .aspectRatio(1f)
        )
        return
    }

    val fillColor = heatmapColorForLevel(cell.level)
    val outerShape = RoundedCornerShape(5.dp)
    val innerShape = RoundedCornerShape(4.dp)

    Box(
        modifier = Modifier
            .size(metrics.cellSize)
            .clip(outerShape)
            .background(if (cell.isToday) FitBoardColors.activeCardBg else Color.Transparent)
            .padding(if (cell.isToday) 1.dp else 0.dp)
    )
    {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(innerShape)
                .background(fillColor)
        )
    }
}

@Composable
private fun rememberHeatmapLayoutMetrics(maxWidth: Dp): HeatmapLayoutMetrics {
    val availableWidth = (maxWidth - 44.dp).coerceAtLeast(248.dp)
    val cellGap = 2.5.dp
    val baseWeekGap = 3.5.dp
    val rawCellSize = (availableWidth - (baseWeekGap * (HEATMAP_WEEKS - 1))) / HEATMAP_WEEKS
    val cellSize = rawCellSize.coerceIn(16.dp, 20.dp)
    val weekGap = if (HEATMAP_WEEKS > 1) {
        ((availableWidth - (cellSize * HEATMAP_WEEKS)) / (HEATMAP_WEEKS - 1)).coerceIn(3.dp, 5.dp)
    } else {
        0.dp
    }

    return HeatmapLayoutMetrics(
        cellSize = cellSize,
        cellGap = cellGap,
        weekGap = weekGap
    )
}

private fun buildHeatmapModel(
    today: CalendarDay,
    currentState: AppUiState,
    currentHealthState: HealthSummaryUiState,
    historicalRecords: Map<String, StoredDailyRecord>
): HeatmapModel {
    val todayKey = today.toStorageKey()
    val weekDates = buildHeatmapWeekDates(today)
    val weekColumns = weekDates.mapIndexed { index, days ->
        HeatmapWeekColumn(
            days = days.map { date ->
                val dateKey = date.toStorageKey()
                val isToday = dateKey == todayKey
                val isFuture = date > today
                val score = when {
                    isFuture -> null
                    isToday -> buildHeatmapScore(
                        state = currentState,
                        healthState = currentHealthState
                    )
                    else -> historicalRecords[dateKey]?.let { record ->
                        buildHeatmapScore(
                            state = record.toHistoricalAppUiState(referenceState = currentState),
                            healthState = record.healthSummary.toUiState()
                        )
                    }
                }

                HeatmapDayCell(
                    date = date,
                    score = score,
                    level = heatmapLevelForScore(score),
                    isToday = isToday,
                    isFuture = isFuture
                )
            }
        )
    }

    return HeatmapModel(weeks = weekColumns)
}

private fun buildHeatmapWeekDates(today: CalendarDay): List<List<CalendarDay>> {
    val currentWeekStart = today.startOfWeekMonday()
    val firstWeekStart = currentWeekStart.minusDays((HEATMAP_WEEKS - 1) * 7)

    return List(HEATMAP_WEEKS) { weekIndex ->
        List(7) { dayIndex ->
            firstWeekStart.plusDays(weekIndex * 7 + dayIndex)
        }
    }
}

private fun buildHeatmapScore(
    state: AppUiState,
    healthState: HealthSummaryUiState
): Int? {
    if (!hasHeatmapScoreData(state, healthState)) {
        return null
    }

    return buildHealthScoreState(
        state = state,
        healthState = healthState
    ).totalScore.roundToInt().coerceIn(0, 100)
}

private fun hasHeatmapScoreData(
    state: AppUiState,
    healthState: HealthSummaryUiState
): Boolean {
    return healthState.hasTodaySteps ||
        healthState.hasSleepDuration ||
        state.selectedTraining != null ||
        state.checkedSupplements.isNotEmpty()
}

private fun StoredDailyRecord.toHistoricalAppUiState(
    referenceState: AppUiState
): AppUiState {
    return referenceState.copy(
        weightInput = weight.orEmpty(),
        savedWeight = weight,
        selectedTraining = selectedTraining,
        checkedSupplements = selectedSupplements.toSet(),
        note = note,
        isSaved = true
    )
}

private fun heatmapLevelForScore(score: Int?): HeatmapScoreLevel {
    return when {
        score == null -> HeatmapScoreLevel.Empty
        score <= 20 -> HeatmapScoreLevel.Level1
        score <= 40 -> HeatmapScoreLevel.Level2
        score <= 60 -> HeatmapScoreLevel.Level3
        score <= 80 -> HeatmapScoreLevel.Level4
        else -> HeatmapScoreLevel.Level5
    }
}

@Composable
private fun heatmapColorForLevel(level: HeatmapScoreLevel): Color {
    return when (level) {
        HeatmapScoreLevel.Empty -> HeatmapEmptyCellColor
        HeatmapScoreLevel.Level1 -> HeatmapLevel1Color
        HeatmapScoreLevel.Level2 -> HeatmapLevel2Color
        HeatmapScoreLevel.Level3 -> HeatmapLevel3Color
        HeatmapScoreLevel.Level4 -> HeatmapLevel4Color
        HeatmapScoreLevel.Level5 -> HeatmapLevel5Color
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
            summary = if (healthState.hasTodaySteps) null else healthState.statusMessage
        )

        HomeSummaryCard.Sleep -> HomeMetricCard(
            label = "睡眠",
            title = "昨晚睡眠",
            value = if (healthState.hasSleepDuration) formatSleepDuration(healthState.sleepDurationHours) else "暂无",
            unit = "",
            summary = if (healthState.hasSleepDuration) {
                null
            } else {
                healthState.statusMessage
            }
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
                state.checkedSupplements.isEmpty() -> null
                else -> state.checkedSupplements.joinToString("、")
            }
        )

        HomeSummaryCard.Training -> run {
            val summary = homeTrainingSummary(
                state = state,
                healthState = healthState
            )
            HomeStatusCard(
                label = "训练",
                title = "训练情况",
                primary = summary.primary,
                secondary = summary.secondary
            )
        }
    }
}

private data class HomeTrainingSummary(
    val primary: String,
    val secondary: String?
)

private fun homeTrainingSummary(
    state: AppUiState,
    healthState: HealthSummaryUiState
): HomeTrainingSummary {
    if (healthState.hasWorkout && healthState.primaryWorkoutDisplayType().isNotBlank()) {
        val primaryWorkoutType = healthState.primaryWorkoutDisplayType()
        val primaryText = if (primaryWorkoutType == "传统力量训练") {
            val strengthNames = state.trainingItemsIn(TrainingCategory.TraditionalStrengthTraining)
                .map(TrainingItemConfig::name)
                .toSet()
            val selectedStrengthDetail = state.selectedTraining?.takeIf { it in strengthNames }
            selectedStrengthDetail?.let { detail ->
                "传统力量训练：$detail"
            } ?: "传统力量训练"
        } else {
            primaryWorkoutType
        }

        val secondaryText = healthState.additionalWorkoutCount()
            .takeIf { it > 0 }
            ?.let { count -> "另有 $count 项训练" }

        return HomeTrainingSummary(
            primary = primaryText,
            secondary = secondaryText
        )
    }

    return HomeTrainingSummary(
        primary = state.selectedTraining ?: "今日未记录",
        secondary = null
    )
}

@Composable
private fun HomeMetricCard(
    label: String,
    title: String,
    value: String,
    unit: String,
    summary: String?
) {
    FitCard {
        CardLabel(label)
        Spacer(Modifier.height(4.dp))
        CardTitle(title)
        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 36.sp,
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

        if (!summary.isNullOrBlank()) {
            Spacer(Modifier.height(14.dp))
            HomeInfoPanel(text = summary)
        }
    }
}

@Composable
private fun HomeScoreCard(scoreState: HealthScorePageState) {
    FitCard {
        CardLabel("评分")
        Spacer(Modifier.height(4.dp))
        CardTitle("今日健康分")
        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = formatScore(scoreState.totalScore),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FitBoardColors.textPrimary
                )
                Text(
                    text = "/ 100",
                    fontSize = 14.sp,
                    color = FitBoardColors.textSecondary
                )
            }
            ScoreTag(text = scoreState.levelLabel)
        }

    }
}

@Composable
private fun HomeStatusCard(
    label: String,
    title: String,
    primary: String,
    secondary: String?
) {
    FitCard {
        CardLabel(label)
        Spacer(Modifier.height(4.dp))
        CardTitle(title)
        Spacer(Modifier.height(18.dp))

        Text(
            text = primary,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = FitBoardColors.textPrimary
        )

        if (!secondary.isNullOrBlank()) {
            Spacer(Modifier.height(14.dp))
            HomeInfoPanel(text = secondary)
        }
    }
}

@Composable
private fun HomeInfoPanel(
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.innerPanelBg)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = text,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = FitBoardColors.textSecondary
            )
        }
    }
}

@Composable
private fun HomeCardsEmptyState(onEditClick: () -> Unit) {
    FitCard {
        CardTitle("暂无摘要")
        Spacer(Modifier.height(14.dp))
        QuietActionButton(
            label = "编辑",
            onClick = onEditClick
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

        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "编辑摘要",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
        }

        Spacer(Modifier.height(12.dp))

        EditorSectionCard(
            label = "已显示",
            title = "当前摘要卡片",
            countText = "${visibleCards.size} 项"
        ) {
            if (visibleCards.isEmpty()) {
                EmptyEditorState(text = "暂无卡片")
            } else {
                visibleCards.forEachIndexed { index, card ->
                    EditorCardRow(
                        title = card.title,
                        actionLabel = "移除",
                        actionSymbol = "-",
                        onAction = { onRemoveCard(card) }
                    )
                    if (index != visibleCards.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        EditorSectionCard(
            label = "可添加",
            title = "候选摘要卡片",
            countText = "${hiddenCards.size} 项"
        ) {
            if (hiddenCards.isEmpty()) {
                EmptyEditorState(text = "已全部添加")
            } else {
                hiddenCards.forEachIndexed { index, card ->
                    EditorCardRow(
                        title = card.title,
                        actionLabel = "添加",
                        actionSymbol = "+",
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        QuietActionButton(
            label = "完成",
            onClick = onBack
        )
    }
}

@Composable
private fun EditorSectionCard(
    label: String,
    title: String,
    countText: String,
    content: @Composable () -> Unit
) {
    FitCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CardLabel(label)
                CardTitle(title)
            }
            QuietPill(countText)
        }

        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun EditorCardRow(
    title: String,
    actionLabel: String,
    actionSymbol: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.innerPanelBg)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = FitBoardColors.textPrimary
            )
        }

        Spacer(Modifier.width(12.dp))

        EditorActionButton(
            label = actionLabel,
            symbol = actionSymbol,
            onClick = onAction
        )
    }
}

@Composable
private fun EditorActionButton(
    label: String,
    symbol: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(start = 2.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(1.dp, FitBoardColors.activeCardBorder, CircleShape)
                .background(FitBoardColors.activeCardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = FitBoardColors.activeText
            )
        }
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = FitBoardColors.activeText
        )
    }
}

@Composable
private fun EmptyEditorState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(18.dp))
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

@Composable
private fun QuietPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, FitBoardColors.countBadgeBorder, RoundedCornerShape(16.dp))
            .background(FitBoardColors.countBadgeBg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = FitBoardColors.countBadgeText
        )
    }
}

@Composable
private fun QuietActionButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.activeCardBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.activeCardBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = FitBoardColors.activeText
        )
    }
}
