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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    state: AppUiState,
    onStateChange: (AppUiState) -> Unit
) {
    var currentPage by remember { mutableStateOf(SettingsPage.Home) }

    when (currentPage) {
        SettingsPage.Home -> SettingsHomeScreen(
            state = state,
            onNavigate = { currentPage = it }
        )

        SettingsPage.Training -> SettingsSubpageScaffold(
            title = "训练类型",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            TrainingSettingsSection(
                items = state.trainingItems,
                onAdd = { category, name ->
                    if (name.isNotBlank() && name !in state.trainingOptions) {
                        onStateChange(
                            state.copy(
                                trainingItems = state.trainingItems.insertTrainingItem(
                                    TrainingItemConfig(
                                        name = name,
                                        category = category,
                                        defaultDurationMinutes = defaultTrainingDurationFor(name, category)
                                    )
                                )
                            )
                        )
                    }
                },
                onDelete = { item ->
                    if (state.trainingItems.count { it.category == item.category } > 1) {
                        onStateChange(
                            state.copy(
                                trainingItems = state.trainingItems.filterNot { it.name == item.name },
                                selectedTraining = if (state.selectedTraining == item.name) null else state.selectedTraining
                            )
                        )
                    }
                },
                onDurationChange = { item, duration ->
                    onStateChange(
                        state.copy(
                            trainingItems = state.trainingItems.map { current ->
                                if (current.name == item.name) {
                                    current.copy(
                                        defaultDurationMinutes = normalizeTrainingDurationMinutes(duration)
                                    )
                                } else {
                                    current
                                }
                            }
                        )
                    )
                }
            )
        }

        SettingsPage.Supplement -> SettingsSubpageScaffold(
            title = "补剂类型",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            SupplementSettingsSection(
                options = state.supplementOptions,
                onAdd = { name ->
                    if (name.isNotBlank() && name !in state.supplementOptions) {
                        onStateChange(state.copy(supplementOptions = state.supplementOptions + name))
                    }
                },
                onDelete = { name ->
                    if (state.supplementOptions.size > 1) {
                        onStateChange(
                            state.copy(
                                supplementOptions = state.supplementOptions - name,
                                checkedSupplements = state.checkedSupplements - name
                            )
                        )
                    }
                }
            )
        }

        SettingsPage.SleepGoal -> SettingsSubpageScaffold(
            title = "睡眠目标",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            SleepGoalSettingsCard(
                selectedHour = state.sleepGoalHours,
                selectedMinute = state.sleepGoalMinutes,
                onSelectHour = { onStateChange(state.copy(sleepGoalHours = it)) },
                onSelectMinute = { onStateChange(state.copy(sleepGoalMinutes = it)) }
            )
        }

        SettingsPage.StepGoal -> SettingsSubpageScaffold(
            title = "步数目标",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            StepGoalSettingsCard(
                selectedStepGoal = state.stepGoal,
                onSelectStepGoal = { onStateChange(state.copy(stepGoal = it)) }
            )
        }

        SettingsPage.Style -> SettingsSubpageScaffold(
            title = "风格",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            StyleSettingsSection(
                themeMode = state.themeMode,
                onThemeChange = { onStateChange(state.copy(themeMode = it)) }
            )
        }

        SettingsPage.LocalData -> SettingsSubpageScaffold(
            title = "本地数据",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            LocalDataSection()
        }

        SettingsPage.About -> SettingsSubpageScaffold(
            title = "关于",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            AboutSection()
        }
    }
}

private enum class SettingsPage {
    Home,
    Training,
    Supplement,
    SleepGoal,
    StepGoal,
    Style,
    LocalData,
    About
}

@Composable
private fun SettingsHomeScreen(
    state: AppUiState,
    onNavigate: (SettingsPage) -> Unit
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
                text = "设置",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
        }

        Spacer(Modifier.height(20.dp))

        SettingsEntryCard(
            title = "训练类型",
            onClick = { onNavigate(SettingsPage.Training) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "补剂类型",
            onClick = { onNavigate(SettingsPage.Supplement) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "睡眠目标",
            value = formatSleepGoal(state.sleepGoalHours, state.sleepGoalMinutes),
            onClick = { onNavigate(SettingsPage.SleepGoal) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "步数目标",
            value = "${state.stepGoal}步",
            onClick = { onNavigate(SettingsPage.StepGoal) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "风格",
            value = state.themeMode.title,
            onClick = { onNavigate(SettingsPage.Style) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "本地数据",
            onClick = { onNavigate(SettingsPage.LocalData) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "关于",
            onClick = { onNavigate(SettingsPage.About) }
        )

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SettingsSubpageScaffold(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    content: @Composable () -> Unit
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
            SettingsSubpageBackButton(onBack = onBack)
            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = FitBoardColors.textSecondary
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        content()
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SettingsSubpageBackButton(onBack: () -> Unit) {
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
internal fun TrainingSettingsSection(
    items: List<TrainingItemConfig>,
    onAdd: (TrainingCategory, String) -> Unit,
    onDelete: (TrainingItemConfig) -> Unit,
    onDurationChange: (TrainingItemConfig, Int) -> Unit
) {
    var strengthDraft by remember { mutableStateOf("") }

    val strengthItems = remember(items) {
        items.filter { it.category == TrainingCategory.TraditionalStrengthTraining }
    }
    val otherItems = remember(items) {
        items.filter { it.category == TrainingCategory.OtherWorkout }
    }

    TrainingGroupSettingsCard(
        category = TrainingCategory.TraditionalStrengthTraining,
        items = strengthItems,
        draft = strengthDraft,
        placeholder = "新增力量训练项",
        showAddInput = true,
        allowDelete = true,
        onDraftChange = { strengthDraft = it },
        onAdd = {
            onAdd(TrainingCategory.TraditionalStrengthTraining, strengthDraft.trim())
            strengthDraft = ""
        },
        onDelete = onDelete,
        onDurationChange = onDurationChange
    )

    Spacer(Modifier.height(12.dp))

    TrainingGroupSettingsCard(
        category = TrainingCategory.OtherWorkout,
        items = otherItems,
        draft = "",
        placeholder = "",
        showAddInput = false,
        allowDelete = false,
        onDraftChange = {},
        onAdd = {},
        onDelete = onDelete,
        onDurationChange = onDurationChange
    )
}

@Composable
private fun TrainingGroupSettingsCard(
    category: TrainingCategory,
    items: List<TrainingItemConfig>,
    draft: String,
    placeholder: String,
    showAddInput: Boolean,
    allowDelete: Boolean,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (TrainingItemConfig) -> Unit,
    onDurationChange: (TrainingItemConfig, Int) -> Unit
) {
    SettingsCard(label = "训练", title = category.title) {
        if (showAddInput) {
            AddInputRow(
                value = draft,
                placeholder = placeholder,
                addLabel = "新增",
                onValueChange = onDraftChange,
                onAdd = onAdd
            )

            Spacer(Modifier.height(10.dp))
        }

        if (items.isEmpty()) {
            EmptyHint("暂无训练项")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    TrainingSettingsItemRow(
                        item = item,
                        canDelete = allowDelete && items.size > 1,
                        onDelete = { onDelete(item) },
                        onDurationDecrease = {
                            onDurationChange(
                                item,
                                item.defaultDurationMinutes - TRAINING_DURATION_STEP_MINUTES
                            )
                        },
                        onDurationIncrease = {
                            onDurationChange(
                                item,
                                item.defaultDurationMinutes + TRAINING_DURATION_STEP_MINUTES
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun SupplementSettingsSection(
    options: List<String>,
    onAdd: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }

    SettingsCard(label = "补剂", title = "补剂类型") {
        AddInputRow(
            value = draft,
            placeholder = "新增补剂名称",
            addLabel = "新增",
            onValueChange = { draft = it },
            onAdd = {
                onAdd(draft.trim())
                draft = ""
            }
        )

        Spacer(Modifier.height(10.dp))

        if (options.isEmpty()) {
            EmptyHint("暂无补剂项")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { name ->
                    SettingsItemRow(
                        name = name,
                        canDelete = options.size > 1,
                        onDelete = { onDelete(name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalDataSection() {
    SettingsCard(label = "本地", title = "导入 / 导出") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlaceholderActionButton(
                label = "导出",
                modifier = Modifier.weight(1f)
            )
            PlaceholderActionButton(
                label = "导入",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AboutSection() {
    SettingsCard(label = "关于", title = "应用") {
        AboutRow(key = "名称", value = "FitBoard")
        Spacer(Modifier.height(8.dp))
        AboutRow(key = "技术", value = "Compose Multiplatform")
    }
}

@Composable
private fun StyleSettingsSection(
    themeMode: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit
) {
    SettingsCard(label = "主题", title = "主题") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppThemeMode.entries.forEach { option ->
                StyleOptionCard(
                    title = option.title,
                    selected = option == themeMode,
                    swatches = previewSwatchesForTheme(option),
                    onClick = { onThemeChange(option) }
                )
            }
        }
    }
}

@Composable
private fun SleepGoalSettingsCard(
    selectedHour: Int,
    selectedMinute: Int,
    onSelectHour: (Int) -> Unit,
    onSelectMinute: (Int) -> Unit
) {
    SettingsCard(label = "睡眠", title = "目标时长") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GoalPickerColumn(
                title = "小时",
                selectedValue = selectedHour,
                options = (4..12).toList(),
                optionLabel = { "${it}小时" },
                onSelect = onSelectHour,
                modifier = Modifier.weight(1f)
            )
            GoalPickerColumn(
                title = "分钟",
                selectedValue = selectedMinute,
                options = listOf(0, 15, 30, 45),
                optionLabel = { "${it}分" },
                onSelect = onSelectMinute,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StepGoalSettingsCard(
    selectedStepGoal: Int,
    onSelectStepGoal: (Int) -> Unit
) {
    SettingsCard(label = "步数", title = "目标步数") {
        GoalPickerColumn(
            title = "步数",
            selectedValue = selectedStepGoal,
            options = (1..30).map { it * 1000 },
            optionLabel = { "${it}步" },
            onSelect = onSelectStepGoal,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun <T> GoalPickerColumn(
    title: String,
    selectedValue: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(16.dp))
            .background(FitBoardColors.innerPanelBg)
            .padding(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = FitBoardColors.textSecondary
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.height(220.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                val selected = option == selectedValue
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (selected) FitBoardColors.activeCardBorder else FitBoardColors.inactiveCardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .background(if (selected) FitBoardColors.activeCardBg else FitBoardColors.inactiveCardBg)
                        .clickable { onSelect(option) }
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = optionLabel(option),
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) FitBoardColors.activeText else FitBoardColors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsEntryCard(
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, FitBoardColors.cardBorder, RoundedCornerShape(20.dp))
            .background(FitBoardColors.cardBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 17.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = FitBoardColors.textPrimary
                )
                if (!value.isNullOrBlank()) {
                    Text(
                        text = value,
                        fontSize = 12.sp,
                        color = FitBoardColors.textHint,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
            text = "›",
            fontSize = 16.sp,
            color = FitBoardColors.textHint
        )
    }
    }
}

@Composable
private fun StyleOptionCard(
    title: String,
    selected: Boolean,
    swatches: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (selected) FitBoardColors.activeCardBorder else FitBoardColors.inactiveCardBorder,
                RoundedCornerShape(18.dp)
            )
            .background(if (selected) FitBoardColors.activeCardBg else FitBoardColors.inactiveCardBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = FitBoardColors.textPrimary
                )
            }
            Spacer(Modifier.width(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                swatches.forEach { color ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, FitBoardColors.cardBorder, RoundedCornerShape(6.dp))
                            .background(color)
                            .width(16.dp)
                            .height(16.dp)
                    )
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FitBoardColors.badgeActiveBg)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "当前",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = FitBoardColors.badgeActiveText
                        )
                    }
                }
            }
        }
    }
}

private fun previewSwatchesForTheme(themeMode: AppThemeMode): List<Color> =
    when (themeMode) {
        AppThemeMode.SoftGreen -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF2F6FF),
            Color(0xFF2E6EDC)
        )
        AppThemeMode.White -> listOf(
            Color(0xFFFEFFFF),
            Color(0xFFF1F6FF),
            Color(0xFF2A65C8)
        )
        AppThemeMode.Dark -> listOf(
            Color(0xFF080808),
            Color(0xFF161616),
            Color(0xFF4D7BD9)
        )
    }

private fun formatSleepGoal(hour: Int, minute: Int): String =
    if (minute == 0) {
        "${hour}小时"
    } else {
        "${hour}小时${minute}分"
    }

@Composable
private fun SettingsCard(
    label: String,
    title: String,
    content: @Composable () -> Unit
) {
    FitCard {
        CardLabel(label)
        Spacer(Modifier.height(2.dp))
        CardTitle(title)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun AddInputRow(
    value: String,
    placeholder: String,
    addLabel: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(placeholder, color = FitBoardColors.textHint, fontSize = 13.sp)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
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
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FitBoardColors.buttonGreen,
                contentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(addLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SettingsItemRow(
    name: String,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.inactiveCardBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.inactiveCardBg)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = FitBoardColors.textPrimary
            )
        }
        Spacer(Modifier.width(8.dp))
        if (canDelete) {
            DeleteButton(onDelete = onDelete)
        }
    }
}

@Composable
private fun TrainingSettingsItemRow(
    item: TrainingItemConfig,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onDurationDecrease: () -> Unit,
    onDurationIncrease: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.inactiveCardBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.inactiveCardBg)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = FitBoardColors.textPrimary
            )

            if (canDelete) {
                DeleteButton(onDelete = onDelete)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DurationStepperActionButton(
                label = "−",
                enabled = item.defaultDurationMinutes > TRAINING_DURATION_MIN_MINUTES,
                onClick = onDurationDecrease
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, FitBoardColors.countBadgeBorder, RoundedCornerShape(16.dp))
                    .background(FitBoardColors.innerPanelBg)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.defaultDurationMinutes.formatTrainingDuration(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FitBoardColors.textPrimary
                )
            }

            DurationStepperActionButton(
                label = "+",
                enabled = item.defaultDurationMinutes < TRAINING_DURATION_MAX_MINUTES,
                onClick = onDurationIncrease
            )
        }
    }
}

@Composable
private fun DurationStepperActionButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (enabled) FitBoardColors.activeCardBorder else FitBoardColors.inactiveCardBorder,
                RoundedCornerShape(14.dp)
            )
            .background(if (enabled) FitBoardColors.activeCardBg else FitBoardColors.inactiveCardBg)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) FitBoardColors.activeText else FitBoardColors.textHint
        )
    }
}

@Composable
private fun DeleteButton(onDelete: () -> Unit) {
    Button(
        onClick = onDelete,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FitBoardColors.dangerBg,
            contentColor = FitBoardColors.dangerText,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text("删除", fontSize = 12.sp)
    }
}

private fun List<TrainingItemConfig>.insertTrainingItem(item: TrainingItemConfig): List<TrainingItemConfig> {
    val insertionIndex = indexOfLast { it.category == item.category }
    if (insertionIndex == -1) {
        return this + item
    }

    return toMutableList().apply {
        add(insertionIndex + 1, item)
    }
}

private fun Int.formatTrainingDuration(): String {
    return "${this}分钟"
}

@Composable
private fun PlaceholderActionButton(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.cardBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.cardBg)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = FitBoardColors.textHint
        )
    }
}

@Composable
private fun AboutRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(key, fontSize = 13.sp, color = FitBoardColors.textSecondary)
        Text(value, fontSize = 13.sp, color = FitBoardColors.textPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.innerPanelBg)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(text, fontSize = 13.sp, color = FitBoardColors.textHint)
    }
}
