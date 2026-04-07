package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max

private enum class WeightTrendRange(
    val label: String,
    val days: Int
) {
    Last7Days("7天", 7),
    Last30Days("30天", 30)
}

private data class WeightSummaryStats(
    val currentWeekAverage: Double?,
    val previousWeekAverage: Double?,
    val weekOverWeekChange: Double?,
    val recent7DayChange: Double?,
    val currentWeekHigh: Double?,
    val currentWeekLow: Double?
)

@Composable
internal fun WeightHistoryScreen(
    healthState: HealthSummaryUiState,
    todayDate: CalendarDay,
    onBack: () -> Unit
) {
    var selectedRange by remember { mutableStateOf(WeightTrendRange.Last7Days) }
    val weightEntries = remember(healthState.weightHistoryRaw) {
        healthState.weightHistoryEntries()
    }
    val trendEntries = remember(weightEntries, todayDate, selectedRange) {
        weightEntries.entriesInRange(
            endInclusive = todayDate,
            days = selectedRange.days
        )
    }
    val summaryStats = remember(weightEntries, todayDate) {
        buildWeightSummaryStats(
            entries = weightEntries,
            todayDate = todayDate
        )
    }
    val recentEntries = remember(weightEntries) {
        weightEntries.sortedByDescending(WeightHistoryEntry::day).take(7)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        Column(Modifier.padding(horizontal = 4.dp)) {
            WeightHistoryBackButton(onBack = onBack)
            Spacer(Modifier.height(14.dp))
            Text(
                text = "体重",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
        }

        Spacer(Modifier.height(20.dp))

        WeightTodayCard(healthState = healthState)

        Spacer(Modifier.height(12.dp))

        WeightTrendCard(
            selectedRange = selectedRange,
            entries = trendEntries,
            todayDate = todayDate,
            onRangeChange = { selectedRange = it }
        )

        Spacer(Modifier.height(12.dp))

        WeightSummaryCard(summaryStats = summaryStats)

        Spacer(Modifier.height(12.dp))

        WeightRecentRecordsCard(entries = recentEntries)

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun WeightHistoryBackButton(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.countBadgeBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.cardBg)
            .clickable { onBack() }
            .defaultMinSize(minHeight = 42.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "‹",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = FitBoardColors.textPrimary
        )
        Text(
            text = "返回",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = FitBoardColors.textPrimary
        )
    }
}

@Composable
private fun WeightTodayCard(healthState: HealthSummaryUiState) {
    val todayWeightText = healthState.formattedTodayWeightTextOrNull()

    FitCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CardLabel("今日体重")
            Text(
                text = todayWeightText ?: "暂无今日体重",
                fontSize = if (todayWeightText != null) 34.sp else 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )

            if (todayWeightText != null) {
                Text(
                    text = "今日最新",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FitBoardColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun WeightTrendCard(
    selectedRange: WeightTrendRange,
    entries: List<WeightHistoryEntry>,
    todayDate: CalendarDay,
    onRangeChange: (WeightTrendRange) -> Unit
) {
    FitCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardLabel("趋势")
            WeightTrendRangeSelector(
                selectedRange = selectedRange,
                onRangeChange = onRangeChange
            )
        }

        Spacer(Modifier.height(14.dp))

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(18.dp))
                    .background(FitBoardColors.innerPanelBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "近${selectedRange.label}暂无体重记录",
                    fontSize = 14.sp,
                    color = FitBoardColors.textSecondary
                )
            }
        } else {
            WeightTrendChart(
                entries = entries,
                startDay = todayDate.minusDays(selectedRange.days - 1),
                endDay = todayDate
            )
        }
    }
}

@Composable
private fun WeightTrendRangeSelector(
    selectedRange: WeightTrendRange,
    onRangeChange: (WeightTrendRange) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WeightTrendRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) FitBoardColors.activeCardBorder else FitBoardColors.cardBorder,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(if (isSelected) FitBoardColors.activeCardBg else FitBoardColors.cardBg)
                    .clickable { onRangeChange(range) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = range.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) FitBoardColors.badgeActiveText else FitBoardColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun WeightTrendChart(
    entries: List<WeightHistoryEntry>,
    startDay: CalendarDay,
    endDay: CalendarDay
) {
    val lineColor = FitBoardColors.textPrimary
    val gridColor = FitBoardColors.cardBorder
    val pointColor = FitBoardColors.cardBg

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(18.dp))
                .background(FitBoardColors.innerPanelBg)
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val leftPadding = 2.dp.toPx()
                val rightPadding = 2.dp.toPx()
                val topPadding = 4.dp.toPx()
                val bottomPadding = 4.dp.toPx()
                val chartWidth = size.width - leftPadding - rightPadding
                val chartHeight = size.height - topPadding - bottomPadding
                val totalDays = max(startDay.daysUntil(endDay), 1)
                val rawMinWeight = entries.minOf(WeightHistoryEntry::kilograms)
                val rawMaxWeight = entries.maxOf(WeightHistoryEntry::kilograms)
                val weightSpread = max(rawMaxWeight - rawMinWeight, 0.4)
                val verticalPadding = max(weightSpread * 0.18, 0.2)
                val minWeight = rawMinWeight - verticalPadding
                val maxWeight = rawMaxWeight + verticalPadding

                listOf(0f, 0.5f, 1f).forEach { fraction ->
                    val y = topPadding + chartHeight * fraction
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPadding, y),
                        end = Offset(size.width - rightPadding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val points = entries.map { entry ->
                    val xFraction = startDay.daysUntil(entry.day).toFloat() / totalDays.toFloat()
                    val normalizedWeight = ((entry.kilograms - minWeight) / (maxWeight - minWeight))
                        .coerceIn(0.0, 1.0)
                    Offset(
                        x = leftPadding + chartWidth * xFraction,
                        y = topPadding + chartHeight * (1f - normalizedWeight.toFloat())
                    )
                }

                if (points.size == 1) {
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = points.first()
                    )
                } else {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { point ->
                            lineTo(point.x, point.y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(
                            width = 2.2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    points.forEach { point ->
                        drawCircle(
                            color = pointColor,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 2.4.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = startDay.formatMonthDayText(),
                fontSize = 12.sp,
                color = FitBoardColors.textHint
            )
            Text(
                text = endDay.formatMonthDayText(),
                fontSize = 12.sp,
                color = FitBoardColors.textHint
            )
        }
    }
}

@Composable
private fun WeightSummaryCard(summaryStats: WeightSummaryStats) {
    FitCard {
        CardLabel("统计")
        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SummaryMetricRow(
                label = "本周平均",
                value = summaryStats.currentWeekAverage.toWeightTextOrPlaceholder()
            )
            SummaryDivider()
            SummaryMetricRow(
                label = "较上周",
                value = summaryStats.weekOverWeekChange.toWeightDeltaTextOrPlaceholder()
            )
            SummaryDivider()
            SummaryMetricRow(
                label = "7天变化",
                value = summaryStats.recent7DayChange.toWeightDeltaTextOrPlaceholder()
            )
            SummaryDivider()
            SummaryMetricRow(
                label = "本周最高",
                value = summaryStats.currentWeekHigh.toWeightTextOrPlaceholder()
            )
            SummaryDivider()
            SummaryMetricRow(
                label = "本周最低",
                value = summaryStats.currentWeekLow.toWeightTextOrPlaceholder()
            )
        }
    }
}

@Composable
private fun SummaryMetricRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = FitBoardColors.textSecondary
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = FitBoardColors.textPrimary
        )
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FitBoardColors.cardBorder)
    )
}

@Composable
private fun WeightRecentRecordsCard(entries: List<WeightHistoryEntry>) {
    FitCard {
        CardLabel("最近记录")
        Spacer(Modifier.height(14.dp))

        if (entries.isEmpty()) {
            Text(
                text = "暂无体重记录",
                fontSize = 14.sp,
                color = FitBoardColors.textSecondary
            )
            return@FitCard
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    SummaryDivider()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.day.formatMonthDayText(),
                        fontSize = 15.sp,
                        color = FitBoardColors.textPrimary
                    )
                    Text(
                        text = "${formatWeightKilograms(entry.kilograms)} kg",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = FitBoardColors.textPrimary
                    )
                }
            }
        }
    }
}

private fun List<WeightHistoryEntry>.entriesInRange(
    endInclusive: CalendarDay,
    days: Int
): List<WeightHistoryEntry> {
    val startInclusive = endInclusive.minusDays(days - 1)
    return filter { entry ->
        entry.day >= startInclusive && entry.day <= endInclusive
    }.sortedBy(WeightHistoryEntry::day)
}

private fun buildWeightSummaryStats(
    entries: List<WeightHistoryEntry>,
    todayDate: CalendarDay
): WeightSummaryStats {
    val thisWeekStart = todayDate.startOfWeekMonday()
    val lastWeekStart = thisWeekStart.minusDays(7)
    val lastWeekEnd = thisWeekStart.minusDays(1)
    val recent7DayEntries = entries.entriesInRange(
        endInclusive = todayDate,
        days = 7
    )
    val thisWeekEntries = entries.filter { entry ->
        entry.day >= thisWeekStart && entry.day <= todayDate
    }
    val lastWeekEntries = entries.filter { entry ->
        entry.day >= lastWeekStart && entry.day <= lastWeekEnd
    }
    val thisWeekAverage = thisWeekEntries.averageKilograms()
    val lastWeekAverage = lastWeekEntries.averageKilograms()

    return WeightSummaryStats(
        currentWeekAverage = thisWeekAverage,
        previousWeekAverage = lastWeekAverage,
        weekOverWeekChange = if (thisWeekAverage != null && lastWeekAverage != null) {
            thisWeekAverage - lastWeekAverage
        } else {
            null
        },
        recent7DayChange = if (recent7DayEntries.size >= 2) {
            recent7DayEntries.last().kilograms - recent7DayEntries.first().kilograms
        } else {
            null
        },
        currentWeekHigh = thisWeekEntries.maxOfOrNull(WeightHistoryEntry::kilograms),
        currentWeekLow = thisWeekEntries.minOfOrNull(WeightHistoryEntry::kilograms)
    )
}

private fun List<WeightHistoryEntry>.averageKilograms(): Double? =
    if (isEmpty()) null else map(WeightHistoryEntry::kilograms).average()

private fun Double?.toWeightTextOrPlaceholder(): String =
    this?.let { "${formatWeightKilograms(it)} kg" } ?: "暂无"

private fun Double?.toWeightDeltaTextOrPlaceholder(): String =
    this?.let(::formatWeightDeltaText) ?: "暂无"

private fun formatWeightDeltaText(value: Double): String {
    if (value == 0.0) {
        return "0.0 kg"
    }

    val sign = if (value > 0.0) "+" else "-"
    return "$sign${formatWeightKilograms(abs(value))} kg"
}
