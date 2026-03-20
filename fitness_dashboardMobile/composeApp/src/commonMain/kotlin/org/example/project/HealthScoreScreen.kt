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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private const val SLEEP_MAX_SCORE = 40
private const val STEP_MAX_SCORE = 25
private const val TRAINING_MAX_SCORE = 20
private const val SUPPLEMENT_MAX_SCORE = 15

@Composable
fun HealthScoreScreen(
    state: AppUiState,
    today: String
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = "健康评分",
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
                    text = "当天记录与健康数据构成",
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

        TotalScoreCard(scoreState = scoreState, today = today)
        Spacer(Modifier.height(12.dp))

        DimensionScoresCard(scoreState = scoreState)
        Spacer(Modifier.height(12.dp))

        ScoreCompositionCard(scoreState = scoreState)
        Spacer(Modifier.height(12.dp))

        FitCard {
            CardLabel("说明")
            Spacer(Modifier.height(2.dp))
            CardTitle("评分说明")
            Spacer(Modifier.height(12.dp))
            Text(
                text = "评分用于展示当天记录与健康数据构成。",
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = FitBoardColors.textSecondary
            )
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun TotalScoreCard(
    scoreState: HealthScorePageState,
    today: String
) {
    FitCard {
        CardLabel("总分")
        Spacer(Modifier.height(2.dp))
        CardTitle("今日健康评分")
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = formatScore(scoreState.totalScore),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FitBoardColors.textPrimary
                )
                Text(
                    text = "/ 100",
                    fontSize = 14.sp,
                    color = FitBoardColors.textSecondary
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreTag(text = scoreState.levelLabel)
                Text(
                    text = today,
                    fontSize = 13.sp,
                    color = FitBoardColors.textHint
                )
            }
        }
    }
}

@Composable
private fun DimensionScoresCard(scoreState: HealthScorePageState) {
    FitCard {
        CardLabel("维度")
        Spacer(Modifier.height(2.dp))
        CardTitle("四维分数")
        Spacer(Modifier.height(12.dp))

        scoreState.items.forEachIndexed { index, item ->
            ScoreDimensionRow(item = item)
            if (index != scoreState.items.lastIndex) {
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ScoreDimensionRow(item: HealthScoreItemState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(16.dp))
            .background(FitBoardColors.innerPanelBg)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = FitBoardColors.textPrimary
                )
                Text(
                    text = "${formatScore(item.score)} / ${item.maxScore}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FitBoardColors.activeText
                )
            }

            Text(
                text = item.summary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = FitBoardColors.textSecondary
            )
        }
    }
}

@Composable
private fun ScoreCompositionCard(scoreState: HealthScorePageState) {
    FitCard {
        CardLabel("构成")
        Spacer(Modifier.height(2.dp))
        CardTitle("评分构成")
        Spacer(Modifier.height(12.dp))

        scoreState.items.forEachIndexed { index, item ->
            ScoreCompositionRow(
                label = item.label,
                weightText = item.weightText,
                value = "${formatScore(item.score)} / ${item.maxScore}"
            )
            if (index != scoreState.items.lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ScoreCompositionRow(
    label: String,
    weightText: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = FitBoardColors.textPrimary,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = weightText,
            fontSize = 12.sp,
            color = FitBoardColors.textHint,
            modifier = Modifier.width(52.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            color = FitBoardColors.textSecondary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ScoreTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, FitBoardColors.countBadgeBorder, RoundedCornerShape(20.dp))
            .background(FitBoardColors.countBadgeBg)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = FitBoardColors.countBadgeText
        )
    }
}

private data class HealthScorePageState(
    val totalScore: Double,
    val levelLabel: String,
    val items: List<HealthScoreItemState>
)

private data class HealthScoreItemState(
    val label: String,
    val score: Double,
    val maxScore: Int,
    val summary: String,
    val weightText: String
)

private fun buildHealthScoreState(
    state: AppUiState,
    healthState: HealthSummaryUiState
): HealthScorePageState {
    val sleepScore = calculateSleepScore(healthState, state)
    val stepScore = calculateStepScore(healthState, state)
    val trainingScore = calculateTrainingScore(state.selectedTraining).toDouble()
    val supplementScore = calculateSupplementScore(
        checkedCount = state.checkedSupplements.size,
        totalCount = state.supplementOptions.size
    ).toDouble()

    val items = listOf(
        HealthScoreItemState(
            label = "睡眠",
            score = sleepScore,
            maxScore = SLEEP_MAX_SCORE,
            summary = if (healthState.hasSleepDuration) {
                formatSleepDuration(healthState.sleepDurationHours)
            } else {
                "暂无睡眠数据"
            },
            weightText = "40%"
        ),
        HealthScoreItemState(
            label = "步数",
            score = stepScore,
            maxScore = STEP_MAX_SCORE,
            summary = if (healthState.hasTodaySteps) {
                "${healthState.todaySteps} 步"
            } else {
                "暂无步数数据"
            },
            weightText = "25%"
        ),
        HealthScoreItemState(
            label = "训练",
            score = trainingScore,
            maxScore = TRAINING_MAX_SCORE,
            summary = state.selectedTraining?.let { "已记录 · $it" } ?: "未训练",
            weightText = "20%"
        ),
        HealthScoreItemState(
            label = "补剂",
            score = supplementScore,
            maxScore = SUPPLEMENT_MAX_SCORE,
            summary = buildSupplementSummary(
                checkedCount = state.checkedSupplements.size,
                totalCount = state.supplementOptions.size
            ),
            weightText = "15%"
        )
    )

    val totalScore = items.sumOf { it.score }

    return HealthScorePageState(
        totalScore = totalScore,
        levelLabel = levelForScore(totalScore.roundToInt()),
        items = items
    )
}

private fun calculateSleepScore(
    healthState: HealthSummaryUiState,
    state: AppUiState
): Double {
    if (!healthState.hasSleepDuration) return 0.0

    val targetMinutes = state.sleepGoalHours * 60 + state.sleepGoalMinutes
    if (targetMinutes <= 0) return 0.0

    val actualMinutes = (healthState.sleepDurationHours * 60.0).coerceAtLeast(0.0)
    return SLEEP_MAX_SCORE * (actualMinutes / targetMinutes.toDouble()).coerceAtMost(1.0)
}

private fun calculateStepScore(
    healthState: HealthSummaryUiState,
    state: AppUiState
): Double {
    if (!healthState.hasTodaySteps) return 0.0

    val targetSteps = state.stepGoal
    if (targetSteps <= 0) return 0.0

    return STEP_MAX_SCORE * (
        healthState.todaySteps.toDouble() / targetSteps.toDouble()
        ).coerceAtMost(1.0)
}

private fun calculateTrainingScore(training: String?): Int {
    val value = training?.trim().orEmpty()
    if (value.isEmpty()) return 8

    return when {
        value == "休息" || value.contains("休息") -> 12
        value == "有氧" || value.contains("有氧") -> 14
        value in setOf("胸", "背", "腿", "肩", "手臂") -> 18
        value.contains("力量") || value.contains("胸") || value.contains("背") ||
            value.contains("腿") || value.contains("肩") || value.contains("手臂") -> 18
        else -> 14
    }
}

private fun calculateSupplementScore(
    checkedCount: Int,
    totalCount: Int
): Int {
    if (checkedCount <= 0 || totalCount <= 0) return 3

    val completion = checkedCount.toDouble() / totalCount.toDouble()
    return when {
        completion <= 0.0 -> 3
        completion <= 0.4 -> 6
        completion <= 0.7 -> 10
        else -> 15
    }
}

private fun buildSupplementSummary(
    checkedCount: Int,
    totalCount: Int
): String {
    if (totalCount <= 0) return "暂无补剂配置"

    val percentage = ((checkedCount.toDouble() / totalCount.toDouble()) * 100).roundToInt()
        .coerceIn(0, 100)
    return "$checkedCount/$totalCount · $percentage%"
}

private fun levelForScore(score: Int): String =
    when {
        score >= 85 -> "优秀"
        score >= 70 -> "良好"
        score >= 55 -> "一般"
        else -> "较差"
    }

private fun formatScore(score: Double): String {
    val rounded = (score * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.roundToInt().toString()
    } else {
        rounded.toString()
    }
}
