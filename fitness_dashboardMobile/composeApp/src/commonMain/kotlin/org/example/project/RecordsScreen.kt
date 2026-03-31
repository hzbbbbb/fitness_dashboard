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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecordsScreen(
    state: AppUiState,
    today: String,
    onWeightChange: (String) -> Unit,
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

            RecordCompletenessBanner(state = state)
            Spacer(Modifier.height(12.dp))

            WeightRecordCard(
                weightInput = state.weightInput,
                savedWeight = state.savedWeight,
                onWeightChange = onWeightChange
            )
            Spacer(Modifier.height(12.dp))

            TrainingRecordCard(
                selected = state.selectedTraining,
                options = state.trainingOptions,
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
    weightInput: String,
    savedWeight: String?,
    onWeightChange: (String) -> Unit
) {
    FitCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CardLabel("体重")
                CardTitle("体重录入")
            }
        }

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = weightInput,
            onValueChange = onWeightChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("输入今日体重 (kg)", color = FitBoardColors.textHint, fontSize = 14.sp)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FitBoardColors.activeCardBorder,
                unfocusedBorderColor = FitBoardColors.inactiveCardBorder,
                focusedContainerColor = FitBoardColors.cardBg,
                unfocusedContainerColor = FitBoardColors.inactiveCardBg,
                cursorColor = FitBoardColors.textPrimary,
                focusedTextColor = FitBoardColors.textPrimary,
                unfocusedTextColor = FitBoardColors.textPrimary,
            )
        )

        if (savedWeight != null) {
            Spacer(Modifier.height(10.dp))

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

@Composable
internal fun TrainingRecordCard(
    selected: String?,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    FitCard {
        CardLabel("训练")
        Spacer(Modifier.height(4.dp))
        CardTitle("今日训练")
        Spacer(Modifier.height(14.dp))

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
                    isActive = option == selected,
                    activeLabel = "已选",
                    inactiveLabel = "未选",
                    onClick = { onSelect(option) }
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
