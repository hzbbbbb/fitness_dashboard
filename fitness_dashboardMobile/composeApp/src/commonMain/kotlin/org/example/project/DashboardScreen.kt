package org.example.project

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
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
            orderedCards = state.orderedHomeCards(),
            visibleCards = state.homeVisibleCards,
            onBack = { currentPage = HomePage.Summary },
            onToggleCard = { card ->
                onStateChange(
                    state.copy(
                        homeVisibleCards = if (card in state.homeVisibleCards) {
                            state.homeVisibleCards - card
                        } else {
                            state.homeVisibleCards + card
                        }
                    )
                )
            },
            onReorderCards = { reorderedCards ->
                onStateChange(state.copy(homeCardOrder = reorderedCards))
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
            title = "步数",
            value = if (healthState.hasTodaySteps) "${healthState.todaySteps}" else "暂无",
            unit = if (healthState.hasTodaySteps) "步" else "",
            summary = if (healthState.hasTodaySteps) null else healthState.statusMessage
        )

        HomeSummaryCard.Sleep -> HomeMetricCard(
            title = "睡眠",
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
            title = "补剂",
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
                title = "训练",
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
    title: String,
    value: String,
    unit: String,
    summary: String?
) {
    FitCard {
        HomeSummaryCardTitle(title)
        Spacer(Modifier.height(16.dp))

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
    var playEntryAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        playEntryAnimation = true
    }

    FitCard {
        HomeSummaryCardTitle("健康分")
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatScore(scoreState.totalScore),
                fontSize = 42.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )

            Spacer(Modifier.width(14.dp))

            ScoreOverviewRings(
                items = scoreState.items,
                playEntryAnimation = playEntryAnimation,
                ringSize = 112.dp
            )
        }
    }
}

@Composable
private fun HomeStatusCard(
    title: String,
    primary: String,
    secondary: String?
) {
    FitCard {
        HomeSummaryCardTitle(title)
        Spacer(Modifier.height(16.dp))

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
private fun HomeSummaryCardTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = FitBoardColors.textPrimary
    )
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
    orderedCards: List<HomeSummaryCard>,
    visibleCards: Set<HomeSummaryCard>,
    onBack: () -> Unit,
    onToggleCard: (HomeSummaryCard) -> Unit,
    onReorderCards: (List<HomeSummaryCard>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val rowGapPx = with(LocalDensity.current) { 8.dp.toPx() }
    val defaultRowHeightPx = with(LocalDensity.current) { 60.dp.toPx() }
    val rowHeights = remember { mutableStateMapOf<HomeSummaryCard, Int>() }
    val settlingOffsets = remember { mutableStateMapOf<HomeSummaryCard, Float>() }
    var draggingCard by remember { mutableStateOf<HomeSummaryCard?>(null) }
    var dragStartIndex by remember { mutableStateOf(-1) }
    var dragTargetIndex by remember { mutableStateOf(-1) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    fun rowHeight(card: HomeSummaryCard): Float =
        rowHeights[card]?.toFloat() ?: defaultRowHeightPx

    fun rowExtent(card: HomeSummaryCard): Float =
        rowHeight(card) + rowGapPx

    fun slotTop(index: Int): Float {
        if (index <= 0) {
            return 0f
        }

        var top = 0f
        for (position in 0 until index) {
            top += rowExtent(orderedCards[position])
        }
        return top
    }

    fun calculateTargetIndex(card: HomeSummaryCard, offsetY: Float): Int {
        val startIndex = orderedCards.indexOf(card)
        if (startIndex == -1) {
            return -1
        }

        val draggedCenter = slotTop(startIndex) + offsetY + rowHeight(card) / 2f
        var targetIndex = startIndex

        orderedCards.forEachIndexed { index, otherCard ->
            if (otherCard == card) {
                return@forEachIndexed
            }

            val otherCenter = slotTop(index) + rowHeight(otherCard) / 2f
            if (index > startIndex && draggedCenter > otherCenter) {
                targetIndex = index
            } else if (index < startIndex && draggedCenter < otherCenter) {
                targetIndex = index
            }
        }

        return targetIndex
    }

    fun reorderCards(card: HomeSummaryCard, targetIndex: Int): List<HomeSummaryCard> {
        val startIndex = orderedCards.indexOf(card)
        if (startIndex == -1 || targetIndex == -1 || startIndex == targetIndex) {
            return orderedCards
        }

        return orderedCards.toMutableList().apply {
            removeAt(startIndex)
            add(targetIndex, card)
        }
    }

    fun finishDrag(shouldCommit: Boolean) {
        val card = draggingCard ?: return
        val startIndex = dragStartIndex
        val targetIndex = dragTargetIndex.coerceIn(0, orderedCards.lastIndex)
        val settleOffset = if (shouldCommit) {
            dragOffsetY - (slotTop(targetIndex) - slotTop(startIndex))
        } else {
            dragOffsetY
        }

        if (shouldCommit && targetIndex != startIndex && startIndex != -1) {
            onReorderCards(reorderCards(card, targetIndex))
        }

        draggingCard = null
        dragStartIndex = -1
        dragTargetIndex = -1
        dragOffsetY = 0f

        if (abs(settleOffset) > 0.5f) {
            settlingOffsets[card] = settleOffset
            scope.launch {
                delay(16)
                settlingOffsets[card] = 0f
                delay(220)
                settlingOffsets.remove(card)
            }
        }
    }

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
            label = "摘要",
            title = "卡片顺序",
            countText = "${visibleCards.size}/${orderedCards.size}"
        ) {
            if (orderedCards.isEmpty()) {
                EmptyEditorState(text = "暂无卡片")
            } else {
                orderedCards.forEachIndexed { index, card ->
                    val currentIndex = orderedCards.indexOf(card)
                    val isDragging = draggingCard == card
                    val placeholderShift = when {
                        draggingCard == null || currentIndex == -1 -> 0f
                        dragTargetIndex > dragStartIndex &&
                            currentIndex in (dragStartIndex + 1)..dragTargetIndex -> -rowExtent(draggingCard!!)
                        dragTargetIndex < dragStartIndex &&
                            currentIndex in dragTargetIndex until dragStartIndex -> rowExtent(draggingCard!!)
                        else -> 0f
                    }

                    SortableEditorCardRow(
                        title = card.title,
                        isVisible = card in visibleCards,
                        isDragging = isDragging,
                        dragTranslationY = if (isDragging) dragOffsetY else 0f,
                        placeholderShiftY = if (isDragging) 0f else placeholderShift,
                        settlingShiftY = settlingOffsets[card] ?: 0f,
                        onToggle = { onToggleCard(card) },
                        onHeightChanged = { rowHeights[card] = it },
                        onDragStart = {
                            draggingCard = card
                            dragStartIndex = currentIndex
                            dragTargetIndex = currentIndex
                            dragOffsetY = 0f
                            settlingOffsets.remove(card)
                        },
                        onDrag = { dragDelta ->
                            dragOffsetY += dragDelta
                            dragTargetIndex = calculateTargetIndex(card, dragOffsetY)
                        },
                        onDragEnd = { finishDrag(shouldCommit = true) },
                        onDragCancel = {
                            finishDrag(shouldCommit = false)
                        }
                    )
                    if (index != orderedCards.lastIndex) {
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
private fun SortableEditorCardRow(
    title: String,
    isVisible: Boolean,
    isDragging: Boolean,
    dragTranslationY: Float,
    placeholderShiftY: Float,
    settlingShiftY: Float,
    onToggle: () -> Unit,
    onHeightChanged: (Int) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val rowShape = RoundedCornerShape(18.dp)
    val restingBackground = FitBoardColors.innerPanelBg
    val liftedBackground by animateColorAsState(
        targetValue = if (isDragging) FitBoardColors.cardBg else restingBackground,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "summaryRowBackground"
    )
    val rowBorder by animateColorAsState(
        targetValue = if (isDragging) FitBoardColors.activeCardBorder else FitBoardColors.innerPanelBorder,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "summaryRowBorder"
    )
    val rowScale by animateFloatAsState(
        targetValue = if (isDragging) 1.035f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "summaryRowScale"
    )
    val rowElevation by animateFloatAsState(
        targetValue = if (isDragging) 18f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "summaryRowElevation"
    )
    val passiveTranslationY by animateFloatAsState(
        targetValue = if (abs(settlingShiftY) > 0.5f) settlingShiftY else placeholderShiftY,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 420f
        ),
        label = "summaryRowPassiveTranslation"
    )
    val appliedTranslationY = if (isDragging) dragTranslationY else passiveTranslationY

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 6f else 0f)
            .graphicsLayer {
                translationY = appliedTranslationY
                scaleX = rowScale
                scaleY = rowScale
                shadowElevation = rowElevation
                shape = rowShape
                clip = false
            }
            .clip(rowShape)
            .border(1.dp, rowBorder, rowShape)
            .background(liftedBackground)
            .onSizeChanged { onHeightChanged(it.height) }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DragHandleButton(
            isDragging = isDragging,
            onDragStart = onDragStart,
            onDrag = onDrag,
            onDragEnd = onDragEnd,
            onDragCancel = onDragCancel
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = FitBoardColors.textPrimary
            )
        }

        EditorVisibilityButton(
            isVisible = isVisible,
            onClick = onToggle
        )
    }
}

@Composable
private fun DragHandleButton(
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val handleShape = RoundedCornerShape(12.dp)
    val handleBorder by animateColorAsState(
        targetValue = if (isDragging) FitBoardColors.activeCardBorder else FitBoardColors.innerPanelBorder,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "summaryHandleBorder"
    )
    val handleBackground by animateColorAsState(
        targetValue = if (isDragging) FitBoardColors.activeCardBg else FitBoardColors.cardBg,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "summaryHandleBackground"
    )
    val handleScale by animateFloatAsState(
        targetValue = if (isDragging) 0.98f else 1f,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "summaryHandleScale"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer {
                scaleX = handleScale
                scaleY = handleScale
            }
            .clip(handleShape)
            .border(1.dp, handleBorder, handleShape)
            .background(handleBackground)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragCancel() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.y)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⋮⋮",
            fontSize = 14.sp,
            color = FitBoardColors.textSecondary
        )
    }
}

@Composable
private fun EditorVisibilityButton(
    isVisible: Boolean,
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
                .border(
                    1.dp,
                    if (isVisible) FitBoardColors.activeCardBorder else FitBoardColors.innerPanelBorder,
                    CircleShape
                )
                .background(if (isVisible) FitBoardColors.activeCardBg else FitBoardColors.cardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isVisible) "✓" else "",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = FitBoardColors.activeText
            )
        }
        Text(
            text = if (isVisible) "已显示" else "已隐藏",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isVisible) FitBoardColors.activeText else FitBoardColors.textSecondary
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
