package org.example.project

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun RecordsScreen(
    state: AppUiState,
    healthState: HealthSummaryUiState,
    today: String,
    onWeightCardClick: () -> Unit,
    onTrainingChange: (String?) -> Unit,
    onSupplementsChange: (Set<String>) -> Unit,
    onNoteChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val dismissKeyboardOnScroll = remember(focusManager) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    focusManager.clearFocus(force = true)
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus(force = true)
                })
            }
            .nestedScroll(dismissKeyboardOnScroll)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Column(Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = "今日记录",
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

            RecordCompletenessBanner(
                state = state,
                healthState = healthState
            )
            Spacer(Modifier.height(12.dp))

            WeightRecordCard(
                healthState = healthState,
                onClick = onWeightCardClick
            )
            Spacer(Modifier.height(12.dp))

            TrainingRecordCard(
                selected = state.selectedTraining,
                items = state.trainingItems,
                healthState = healthState,
                onSelect = { option ->
                    val updated = if (state.selectedTraining == option) null else option
                    onTrainingChange(updated)
                }
            )
            Spacer(Modifier.height(12.dp))

            SupplementRecordCard(
                checked = state.checkedSupplements,
                options = state.supplementOptions,
                onToggle = { option ->
                    val updated = if (option in state.checkedSupplements) {
                        state.checkedSupplements - option
                    } else {
                        state.checkedSupplements + option
                    }
                    onSupplementsChange(updated)
                }
            )
            Spacer(Modifier.height(12.dp))

            NotesRecordCard(
                note = state.note,
                onNoteChange = onNoteChange
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun RecordCompletenessBanner(
    state: AppUiState,
    healthState: HealthSummaryUiState
) {
    val hasReadOnlyWorkout = healthState.hasNonStrengthWorkout()
    val hasStrengthSelection = healthState.hasTraditionalStrengthWorkout() && state.selectedTraining != null
    val trainingRecorded = hasReadOnlyWorkout || hasStrengthSelection
    val filledCount = listOfNotNull(
        if (trainingRecorded) "training" else null,
        healthState.formattedTodayWeightValueOrNull(),
        if (state.checkedSupplements.isNotEmpty()) "sup" else null,
        if (state.note.isNotEmpty()) "note" else null
    ).size
    val totalCount = 4
    val isComplete = filledCount == totalCount
    val pct = (filledCount * 100) / totalCount

    val bgColor = if (isComplete) FitBoardColors.activeCardBg else FitBoardColors.innerPanelBg
    val borderColor = if (isComplete) FitBoardColors.activeCardBorder else FitBoardColors.cardBorder
    val dotColor = if (isComplete) FitBoardColors.badgeActiveText else FitBoardColors.badgeInactiveText

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 15.dp)
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
                    text = if (isComplete) "今日记录已填写完整" else "今日记录完成度",
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

@Composable
internal fun WeightRecordCard(
    healthState: HealthSummaryUiState,
    onClick: () -> Unit
) {
    val weightText = healthState.formattedTodayWeightTextOrNull()
    val isLoadingWeight = healthState.authorizationState == HealthAuthorizationState.Loading && weightText == null

    FitCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardLabel("体重")
                Text(
                    text = "查看",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FitBoardColors.textSecondary
                )
            }
            Text(
                text = when {
                    weightText != null -> weightText
                    isLoadingWeight -> "正在读取今日体重"
                    else -> "暂无今日体重"
                },
                fontSize = if (weightText != null) 32.sp else 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )

            if (weightText != null) {
                Text(
                    text = "今日最新",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FitBoardColors.textSecondary
                )
            } else if (isLoadingWeight) {
                Text(
                    text = "Apple Health",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FitBoardColors.textHint
                )
            }
        }
    }
}

@Composable
internal fun TrainingRecordCard(
    selected: String?,
    items: List<TrainingItemConfig>,
    healthState: HealthSummaryUiState,
    onSelect: (String) -> Unit
) {
    val hasDisplayWorkout = healthState.hasWorkout && healthState.primaryWorkoutDisplayType().isNotBlank()
    val hasTraditionalStrengthWorkout = healthState.hasTraditionalStrengthWorkout()
    val primaryWorkoutType = healthState.primaryWorkoutDisplayType()
    val primaryWorkoutDurationMinutes = healthState.primaryWorkoutDisplayDurationMinutes()
    val additionalWorkouts = healthState.additionalWorkoutEntries()
    val strengthItems = remember(items) {
        items.strengthTrainingItems()
    }
    val shouldShowStrengthSelection = hasTraditionalStrengthWorkout
    val workoutDisplayName = when {
        hasTraditionalStrengthWorkout -> "传统力量训练"
        hasDisplayWorkout -> primaryWorkoutType
        else -> primaryWorkoutType
    }

    FitCard {
        CardLabel("训练")
        Spacer(Modifier.height(4.dp))
        CardTitle("今日训练")
        Spacer(Modifier.height(14.dp))

        if (hasDisplayWorkout) {
            WorkoutSummaryPanel(
                workoutType = workoutDisplayName,
                durationMinutes = primaryWorkoutDurationMinutes
            )

            if (additionalWorkouts.isNotEmpty() || shouldShowStrengthSelection) {
                Spacer(Modifier.height(12.dp))
            }
        } else {
            WorkoutSummaryPanel(
                workoutType = "今日无训练",
                durationMinutes = 0.0
            )
        }

        if (additionalWorkouts.isNotEmpty()) {
            AdditionalWorkoutListPanel(items = additionalWorkouts)

            if (shouldShowStrengthSelection) {
                Spacer(Modifier.height(12.dp))
            }
        }

        if (shouldShowStrengthSelection) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(18.dp))
                    .background(FitBoardColors.innerPanelBg)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                strengthItems.forEach { item ->
                    SelectableRecordItem(
                        label = item.name,
                        isActive = item.name == selected,
                        activeLabel = "已选",
                        inactiveLabel = "未选",
                        onClick = { onSelect(item.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdditionalWorkoutListPanel(items: List<WorkoutDisplayEntry>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.innerPanelBg)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "其他训练",
                fontSize = 13.sp,
                color = FitBoardColors.textSecondary
            )

            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.type,
                        fontSize = 15.sp,
                        color = FitBoardColors.textPrimary
                    )
                    Text(
                        text = item.durationMinutes.formatWorkoutDurationText() ?: "0分钟",
                        fontSize = 13.sp,
                        color = FitBoardColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutSummaryPanel(
    workoutType: String,
    durationMinutes: Double
) {
    val durationText = durationMinutes.formatWorkoutDurationText()

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
                text = workoutType,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = FitBoardColors.textPrimary
            )

            if (durationText != null) {
                Text(
                    text = durationText,
                    fontSize = 13.sp,
                    color = FitBoardColors.textSecondary
                )
            }
        }
    }
}

@Composable
internal fun SupplementRecordCard(
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
                Spacer(Modifier.height(4.dp))
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
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(18.dp))
                .background(FitBoardColors.innerPanelBg)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { option ->
                SelectableRecordItem(
                    label = option,
                    isActive = option in checked,
                    activeLabel = "已吃",
                    inactiveLabel = "未吃",
                    onClick = { onToggle(option) }
                )
            }
        }
    }
}

@Composable
private fun SelectableRecordItem(
    label: String,
    isActive: Boolean,
    activeLabel: String,
    inactiveLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isActive) FitBoardColors.activeCardBorder else FitBoardColors.inactiveCardBorder,
                RoundedCornerShape(16.dp)
            )
            .background(if (isActive) FitBoardColors.activeCardBg else FitBoardColors.inactiveCardBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
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

@Composable
internal fun NotesRecordCard(note: String, onNoteChange: (String) -> Unit) {
    FitCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                CardLabel("备注")
                Spacer(Modifier.height(4.dp))
                CardTitle("今日备注")
            }
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
                .defaultMinSize(minHeight = 108.dp),
            placeholder = {
                Text(
                    "备注",
                    color = FitBoardColors.textHint,
                    fontSize = 13.sp
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FitBoardColors.activeCardBorder,
                unfocusedBorderColor = FitBoardColors.inactiveCardBorder,
                focusedContainerColor = FitBoardColors.cardBg,
                unfocusedContainerColor = FitBoardColors.inactiveCardBg,
                cursorColor = FitBoardColors.textPrimary,
                focusedTextColor = FitBoardColors.textPrimary,
                unfocusedTextColor = FitBoardColors.textPrimary,
            ),
            maxLines = 5,
        )
    }
}

private fun HealthSummaryUiState.hasTraditionalStrengthWorkout(): Boolean {
    return hasWorkout && primaryWorkoutDisplayType() == "传统力量训练"
}

private fun HealthSummaryUiState.hasNonStrengthWorkout(): Boolean {
    return hasWorkout && primaryWorkoutDisplayType().isNotBlank() && !hasTraditionalStrengthWorkout()
}

private fun List<TrainingItemConfig>.strengthTrainingItems(): List<TrainingItemConfig> {
    val filtered = filter { it.category == TrainingCategory.TraditionalStrengthTraining }
    return if (filtered.isEmpty()) {
        DEFAULT_TRAINING_ITEMS.filter { it.category == TrainingCategory.TraditionalStrengthTraining }
    } else {
        filtered
    }
}

private fun List<TrainingItemConfig>.otherWorkoutItem(): TrainingItemConfig {
    return firstOrNull { it.category == TrainingCategory.OtherWorkout }
        ?: DEFAULT_TRAINING_ITEMS.first { it.category == TrainingCategory.OtherWorkout }
}
private fun Double.formatWorkoutDurationText(): String? {
    if (this <= 0.0) {
        return null
    }

    return "${roundToInt()}分钟"
}
