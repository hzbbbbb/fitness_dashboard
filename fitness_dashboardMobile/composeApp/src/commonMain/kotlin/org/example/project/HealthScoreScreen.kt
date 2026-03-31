package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private const val SLEEP_MAX_SCORE = 40
private const val STEP_MAX_SCORE = 25
private const val TRAINING_MAX_SCORE = 20
private const val SUPPLEMENT_MAX_SCORE = 15
private val ScoreCardBaseColor = Color(0xFFF3F5F8)
private val ScoreProgressFillColor = Color(0xFFD9E8FF)

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

        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = "健康评分",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                ScoreTag(text = today)
            }
        }

        Spacer(Modifier.height(18.dp))

        TotalScoreCard(scoreState = scoreState, today = today)
        Spacer(Modifier.height(12.dp))

        DimensionScoresCard(scoreState = scoreState)

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
        Spacer(Modifier.height(4.dp))
        CardTitle("今日健康评分")
        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = formatScore(scoreState.totalScore),
                    fontSize = 44.sp,
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
                    fontSize = 12.sp,
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
        Spacer(Modifier.height(4.dp))
        CardTitle("四维分数")
        Spacer(Modifier.height(14.dp))

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
    val progress = (item.score / item.maxScore.toDouble())
        .coerceIn(0.0, 1.0)
        .toFloat()
    val outerShape = RoundedCornerShape(18.dp)
    val innerShape = RoundedCornerShape(17.dp)
    val progressShape = if (progress >= 0.999f) {
        innerShape
    } else {
        RoundedCornerShape(
            topStart = 17.dp,
            bottomStart = 17.dp,
            topEnd = 10.dp,
            bottomEnd = 10.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(outerShape)
            .border(1.dp, FitBoardColors.innerPanelBorder, outerShape)
            .background(ScoreCardBaseColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)
                .clip(innerShape)
                .background(ScoreCardBaseColor)
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(progressShape)
                        .background(ScoreProgressFillColor)
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                lineHeight = 20.sp,
                color = FitBoardColors.textSecondary
            )
        }
    }
}

@Composable
internal fun ScoreTag(text: String) {
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

internal data class HealthScorePageState(
    val totalScore: Double,
    val levelLabel: String,
    val items: List<HealthScoreItemState>
)

internal data class HealthScoreItemState(
    val label: String,
    val score: Double,
    val maxScore: Int,
    val summary: String,
    val weightText: String
)

private data class TrainingScoreState(
    val score: Double,
    val summary: String
)

internal fun buildHealthScoreState(
    state: AppUiState,
    healthState: HealthSummaryUiState
): HealthScorePageState {
    val sleepScore = calculateSleepScore(healthState, state)
    val stepScore = calculateStepScore(healthState, state)
    val trainingScoreState = calculateTrainingScoreState(state, healthState)
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
            score = trainingScoreState.score,
            maxScore = TRAINING_MAX_SCORE,
            summary = trainingScoreState.summary,
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

private fun calculateTrainingScoreState(
    state: AppUiState,
    healthState: HealthSummaryUiState
): TrainingScoreState {
    if (!healthState.hasWorkout) {
        return TrainingScoreState(
            score = 0.0,
            summary = "暂无训练数据"
        )
    }

    val primaryWorkoutType = healthState.primaryWorkoutTypeForScoring()
    val primaryWorkoutMinutes = healthState.primaryWorkoutDurationMinutesForScoring()
    val additionalWorkoutMinutes = healthState.scoreAdditionalWorkoutDurationMinutes.coerceAtLeast(0.0)
    val primaryTargetMinutes = resolvePrimaryTrainingTargetMinutes(
        state = state,
        primaryWorkoutType = primaryWorkoutType
    )
    val additionalTargetMinutes = resolveAdditionalTrainingTargetMinutes(state)
    val displayLabel = resolveTrainingDisplayLabel(
        state = state,
        primaryWorkoutType = primaryWorkoutType
    )

    val primaryScore = calculateTrainingScoreComponent(
        actualMinutes = primaryWorkoutMinutes,
        targetMinutes = primaryTargetMinutes,
        multiplier = 1.0
    )
    val additionalScore = calculateTrainingScoreComponent(
        actualMinutes = additionalWorkoutMinutes,
        targetMinutes = additionalTargetMinutes,
        multiplier = 0.3
    )
    val totalScore = (primaryScore + additionalScore).coerceAtMost(TRAINING_MAX_SCORE.toDouble())

    if (primaryTargetMinutes == null || primaryTargetMinutes <= 0) {
        return TrainingScoreState(
            score = totalScore,
            summary = if (primaryWorkoutMinutes > 0.0) {
                buildTrainingScoreSummary(
                    displayLabel = displayLabel,
                    primaryWorkoutMinutes = primaryWorkoutMinutes,
                    primaryTargetMinutes = null,
                    additionalWorkoutMinutes = additionalWorkoutMinutes
                )
            } else {
                displayLabel
            }
        )
    }

    return TrainingScoreState(
        score = totalScore,
        summary = buildTrainingScoreSummary(
            displayLabel = displayLabel,
            primaryWorkoutMinutes = primaryWorkoutMinutes,
            primaryTargetMinutes = primaryTargetMinutes,
            additionalWorkoutMinutes = additionalWorkoutMinutes
        )
    )
}

private fun resolvePrimaryTrainingTargetMinutes(
    state: AppUiState,
    primaryWorkoutType: String
): Int? {
    if (primaryWorkoutType == TrainingCategory.TraditionalStrengthTraining.title) {
        val selectedStrength = state.selectedTraining
            ?.takeIf { selected ->
                state.trainingItemsIn(TrainingCategory.TraditionalStrengthTraining)
                    .any { it.name == selected }
            }

        return when {
            selectedStrength != null -> state.defaultTrainingDurationFor(selectedStrength)
            else -> state.trainingItemsIn(TrainingCategory.TraditionalStrengthTraining)
                .firstOrNull()
                ?.defaultDurationMinutes
                ?: TrainingCategory.TraditionalStrengthTraining.defaultDurationMinutes
        }
    }

    return resolveAdditionalTrainingTargetMinutes(state)
}

private fun resolveAdditionalTrainingTargetMinutes(
    state: AppUiState
): Int? {
    return state.trainingItemsIn(TrainingCategory.OtherWorkout)
        .firstOrNull()
        ?.defaultDurationMinutes
        ?: TrainingCategory.OtherWorkout.defaultDurationMinutes
}

private fun resolveTrainingDisplayLabel(
    state: AppUiState,
    primaryWorkoutType: String
): String {
    if (primaryWorkoutType == TrainingCategory.TraditionalStrengthTraining.title) {
        val selectedStrength = state.selectedTraining
            ?.takeIf { selected ->
                state.trainingItemsIn(TrainingCategory.TraditionalStrengthTraining)
                    .any { it.name == selected }
            }

        return selectedStrength?.let { detail ->
            "传统力量训练：$detail"
        } ?: "传统力量训练"
    }

    return primaryWorkoutType.ifEmpty { TrainingCategory.OtherWorkout.title }
}

private fun calculateTrainingScoreComponent(
    actualMinutes: Double,
    targetMinutes: Int?,
    multiplier: Double
): Double {
    if (actualMinutes <= 0.0) return 0.0

    val safeTargetMinutes = targetMinutes?.takeIf { it > 0 } ?: return 0.0
    return TRAINING_MAX_SCORE * (actualMinutes / safeTargetMinutes.toDouble())
        .coerceAtMost(1.0) * multiplier
}

private fun buildTrainingScoreSummary(
    displayLabel: String,
    primaryWorkoutMinutes: Double,
    primaryTargetMinutes: Int?,
    additionalWorkoutMinutes: Double
): String {
    val primaryPart = if (primaryTargetMinutes != null && primaryTargetMinutes > 0) {
        "$displayLabel · ${formatMinutes(primaryWorkoutMinutes)}/${primaryTargetMinutes}分钟"
    } else {
        "$displayLabel · ${formatMinutes(primaryWorkoutMinutes)}分钟"
    }

    if (additionalWorkoutMinutes <= 0.0) {
        return primaryPart
    }

    return "$primaryPart + ${formatMinutes(additionalWorkoutMinutes)}分钟"
}

private fun HealthSummaryUiState.primaryWorkoutTypeForScoring(): String {
    return scorePrimaryWorkoutType.trim().ifEmpty { workoutType.trim() }
}

private fun HealthSummaryUiState.primaryWorkoutDurationMinutesForScoring(): Double {
    return when {
        scorePrimaryWorkoutDurationMinutes > 0.0 -> scorePrimaryWorkoutDurationMinutes
        else -> workoutDurationMinutes.coerceAtLeast(0.0)
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

internal fun formatScore(score: Double): String {
    val rounded = (score * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.roundToInt().toString()
    } else {
        rounded.toString()
    }
}

private fun formatMinutes(minutes: Double): String {
    return formatScore(minutes)
}
