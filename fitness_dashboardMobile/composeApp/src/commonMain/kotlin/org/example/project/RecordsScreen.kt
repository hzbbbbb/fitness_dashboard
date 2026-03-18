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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecordsScreen(
    state: AppUiState,
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

        Column(Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = "今日记录",
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
                    text = "当天录入与保存",
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

        RecordCompletenessBanner(state = state)
        Spacer(Modifier.height(12.dp))

        WeightRecordCard(
            weightInput = state.weightInput,
            savedWeight = state.savedWeight,
            onWeightChange = { onStateChange(state.copy(weightInput = it, isSaved = false)) },
            onSave = {
                val trimmed = state.weightInput.trim()
                if (trimmed.isNotEmpty()) {
                    onStateChange(state.copy(savedWeight = trimmed, isSaved = false))
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        TrainingRecordCard(
            selected = state.selectedTraining,
            options = state.trainingOptions,
            onSelect = { option ->
                val updated = if (state.selectedTraining == option) null else option
                onStateChange(state.copy(selectedTraining = updated, isSaved = false))
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
                onStateChange(state.copy(checkedSupplements = updated, isSaved = false))
            }
        )
        Spacer(Modifier.height(12.dp))

        NotesRecordCard(
            note = state.note,
            onNoteChange = { onStateChange(state.copy(note = it, isSaved = false)) }
        )
        Spacer(Modifier.height(20.dp))

        SaveTodayRecordButton(
            isSaved = state.isSaved,
            onClick = { onStateChange(state.copy(isSaved = true)) }
        )
        Spacer(Modifier.height(28.dp))
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

    val bgColor = if (isComplete) FitBoardColors.activeCardBg else Color(0xFFF5F7F2)
    val borderColor = if (isComplete) FitBoardColors.activeCardBorder else FitBoardColors.cardBorder
    val dotColor = if (isComplete) FitBoardColors.badgeActiveText else FitBoardColors.badgeInactiveText

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 14.dp)
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
    onWeightChange: (String) -> Unit,
    onSave: () -> Unit
) {
    FitCard {
        CardLabel("体重")
        Spacer(Modifier.height(2.dp))
        CardTitle("体重录入")
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
                    Text("输入今日体重 (kg)", color = FitBoardColors.textHint, fontSize = 14.sp)
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

        Spacer(Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("当前体重：", fontSize = 13.sp, color = FitBoardColors.textSecondary)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(FitBoardColors.weightSavedBadgeBg)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = savedWeight?.let { "$it kg" } ?: "未保存",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (savedWeight != null) {
                        FitBoardColors.badgeActiveText
                    } else {
                        FitBoardColors.textHint
                    }
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
                Spacer(Modifier.height(2.dp))
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
                    "记录今日状态、训练感受或补充说明...",
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

@Composable
private fun SaveTodayRecordButton(isSaved: Boolean, onClick: () -> Unit) {
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
