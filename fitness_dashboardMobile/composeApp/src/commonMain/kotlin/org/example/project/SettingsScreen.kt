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
            title = "训练类型设置",
            subtitle = "管理记录页可选的训练类型",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            TrainingSettingsSection(
                options = state.trainingOptions,
                onAdd = { name ->
                    if (name.isNotBlank() && name !in state.trainingOptions) {
                        onStateChange(state.copy(trainingOptions = state.trainingOptions + name))
                    }
                },
                onDelete = { name ->
                    if (state.trainingOptions.size > 1) {
                        onStateChange(
                            state.copy(
                                trainingOptions = state.trainingOptions - name,
                                selectedTraining = if (state.selectedTraining == name) null else state.selectedTraining
                            )
                        )
                    }
                }
            )
        }

        SettingsPage.Supplement -> SettingsSubpageScaffold(
            title = "补剂类型设置",
            subtitle = "管理记录页可选的补剂类型",
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
            title = "睡眠目标设置",
            subtitle = "使用预设时长作为评分目标",
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
            title = "步数目标设置",
            subtitle = "使用预设步数作为评分目标",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            StepGoalSettingsCard(
                selectedStepGoal = state.stepGoal,
                onSelectStepGoal = { onStateChange(state.copy(stepGoal = it)) }
            )
        }

        SettingsPage.Style -> SettingsSubpageScaffold(
            title = "风格设置",
            subtitle = "主题模式与热力图颜色预设",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            StyleSettingsSection(
                themeMode = state.themeMode,
                heatmapAccent = state.heatmapAccent,
                onThemeChange = { onStateChange(state.copy(themeMode = it)) },
                onHeatmapAccentChange = { onStateChange(state.copy(heatmapAccent = it)) }
            )
        }

        SettingsPage.LocalData -> SettingsSubpageScaffold(
            title = "本地数据导入与导出",
            subtitle = "当前仅提供入口和占位说明",
            onBack = { currentPage = SettingsPage.Home }
        ) {
            LocalDataSection()
        }

        SettingsPage.About -> SettingsSubpageScaffold(
            title = "关于应用",
            subtitle = "应用信息与当前定位",
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
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "目标、类型、风格与本地说明",
                fontSize = 14.sp,
                color = FitBoardColors.textSecondary
            )
        }

        Spacer(Modifier.height(20.dp))

        SettingsEntryCard(
            title = "训练类型设置",
            description = "管理记录页的训练选项",
            onClick = { onNavigate(SettingsPage.Training) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "补剂类型设置",
            description = "管理记录页的补剂选项",
            onClick = { onNavigate(SettingsPage.Supplement) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "睡眠目标设置",
            description = formatSleepGoal(state.sleepGoalHours, state.sleepGoalMinutes),
            onClick = { onNavigate(SettingsPage.SleepGoal) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "步数目标设置",
            description = "${state.stepGoal}步",
            onClick = { onNavigate(SettingsPage.StepGoal) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "风格设置",
            description = "${state.themeMode.title} · ${state.heatmapAccent.title}热力图",
            onClick = { onNavigate(SettingsPage.Style) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "本地数据导入与导出",
            description = "入口已预留，功能暂未开放",
            onClick = { onNavigate(SettingsPage.LocalData) }
        )
        Spacer(Modifier.height(12.dp))

        SettingsEntryCard(
            title = "关于应用",
            description = "查看应用信息与当前定位",
            onClick = { onNavigate(SettingsPage.About) }
        )

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SettingsSubpageScaffold(
    title: String,
    subtitle: String,
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
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.textPrimary
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = FitBoardColors.textSecondary
            )
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
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(FitBoardColors.badgeActiveBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "‹",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = FitBoardColors.badgeActiveText
            )
        }
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
    options: List<String>,
    onAdd: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }

    SettingsCard(label = "训练", title = "训练类型设置") {
        Text(
            text = "管理记录页可选的训练类型。第一版继续使用本地编辑，不做独立配置流程。",
            fontSize = 13.sp,
            color = FitBoardColors.textSecondary,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(12.dp))

        AddInputRow(
            value = draft,
            placeholder = "新增训练类型",
            addLabel = "新增",
            onValueChange = { draft = it },
            onAdd = {
                onAdd(draft.trim())
                draft = ""
            }
        )

        Spacer(Modifier.height(10.dp))

        if (options.isEmpty()) {
            EmptyHint("当前没有训练类型配置，可先新增一个训练类型。")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { name ->
                    SettingsItemRow(
                        name = name,
                        description = "用于记录页的训练单选列表",
                        canDelete = options.size > 1,
                        onDelete = { onDelete(name) }
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

    SettingsCard(label = "补剂", title = "补剂类型设置") {
        Text(
            text = "管理记录页可选的补剂类型。第一版先保留本地编辑与列表占位。",
            fontSize = 13.sp,
            color = FitBoardColors.textSecondary,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(12.dp))

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
            EmptyHint("当前没有补剂配置，可先新增一个补剂。")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { name ->
                    SettingsItemRow(
                        name = name,
                        description = "用于记录页的补剂多选列表",
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
    SettingsCard(label = "说明", title = "本地数据与导入导出") {
        Text(
            text = "当前记录只保存在本地页面状态中，退出应用后会重置。后续版本再补持久化、导入和导出能力。",
            fontSize = 13.sp,
            color = FitBoardColors.textSecondary,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlaceholderActionButton(
                label = "导出占位",
                modifier = Modifier.weight(1f)
            )
            PlaceholderActionButton(
                label = "导入占位",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AboutSection() {
    SettingsCard(label = "关于", title = "关于应用") {
        AboutRow(key = "应用名称", value = "FitBoard")
        Spacer(Modifier.height(8.dp))
        AboutRow(key = "当前定位", value = "轻量健康记录助手")
        Spacer(Modifier.height(8.dp))
        AboutRow(key = "界面职责", value = "首页概览 / 健康评分 / 记录录入 / 设置配置")
        Spacer(Modifier.height(8.dp))
        AboutRow(key = "技术实现", value = "Compose Multiplatform")
    }
}

@Composable
private fun StyleSettingsSection(
    themeMode: AppThemeMode,
    heatmapAccent: HeatmapAccent,
    onThemeChange: (AppThemeMode) -> Unit,
    onHeatmapAccentChange: (HeatmapAccent) -> Unit
) {
    SettingsCard(label = "主题", title = "App 主题风格") {
        Text(
            text = "淡绿色模式为当前默认主题，后续可继续扩展持久化。",
            fontSize = 13.sp,
            color = FitBoardColors.textSecondary,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppThemeMode.entries.forEach { option ->
                StyleOptionCard(
                    title = if (option == AppThemeMode.SoftGreen) {
                        "${option.title}（默认）"
                    } else {
                        option.title
                    },
                    description = option.subtitle,
                    selected = option == themeMode,
                    swatches = previewSwatchesForTheme(option),
                    onClick = { onThemeChange(option) }
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    SettingsCard(label = "热力图", title = "热力图颜色") {
        Text(
            text = "切换记录格子的强调色，无记录状态仍保持低对比。",
            fontSize = 13.sp,
            color = FitBoardColors.textSecondary,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HeatmapAccent.entries.forEach { option ->
                StyleOptionCard(
                    title = if (option == HeatmapAccent.Green) {
                        "${option.title}（默认）"
                    } else {
                        option.title
                    },
                    description = option.subtitle,
                    selected = option == heatmapAccent,
                    swatches = previewSwatchesForAccent(option),
                    onClick = { onHeatmapAccentChange(option) }
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
    SettingsCard(label = "睡眠目标", title = "目标时长") {
        Text(
            text = "使用预设滚动选择设置睡眠目标。",
            fontSize = 13.sp,
            color = FitBoardColors.textSecondary,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(12.dp))

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

        Spacer(Modifier.height(12.dp))
        EmptyHint("当前目标：${formatSleepGoal(selectedHour, selectedMinute)}")
    }
}

@Composable
private fun StepGoalSettingsCard(
    selectedStepGoal: Int,
    onSelectStepGoal: (Int) -> Unit
) {
    SettingsCard(label = "步数目标", title = "目标步数") {
        Text(
            text = "使用预设滚动选择设置步数目标。",
            fontSize = 13.sp,
            color = FitBoardColors.textSecondary,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(12.dp))

        GoalPickerColumn(
            title = "步数",
            selectedValue = selectedStepGoal,
            options = (1..30).map { it * 1000 },
            optionLabel = { "${it}步" },
            onSelect = onSelectStepGoal,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        EmptyHint("当前目标：${selectedStepGoal}步")
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
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, FitBoardColors.cardBorder, RoundedCornerShape(18.dp))
            .background(FitBoardColors.cardBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp)
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
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = FitBoardColors.textHint,
                    lineHeight = 18.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "›",
                fontSize = 18.sp,
                color = FitBoardColors.textHint
            )
        }
    }
}

@Composable
private fun StyleOptionCard(
    title: String,
    description: String,
    selected: Boolean,
    swatches: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (selected) FitBoardColors.activeCardBorder else FitBoardColors.inactiveCardBorder,
                RoundedCornerShape(16.dp)
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
            Color(0xFFF7F4EA),
            Color(0xFFF9FBF6),
            Color(0xFFDFF0E2)
        )
        AppThemeMode.White -> listOf(
            Color(0xFFFAFAF7),
            Color(0xFFFFFFFF),
            Color(0xFFF0F4F0)
        )
        AppThemeMode.Dark -> listOf(
            Color(0xFF111714),
            Color(0xFF19211D),
            Color(0xFF24342B)
        )
    }

private fun previewSwatchesForAccent(accent: HeatmapAccent): List<Color> =
    when (accent) {
        HeatmapAccent.Green -> listOf(
            Color(0xFFE8EEE4),
            Color(0xFFBED9C1),
            Color(0xFF6AAA7A)
        )
        HeatmapAccent.Blue -> listOf(
            Color(0xFFECEFEA),
            Color(0xFFC6DFEC),
            Color(0xFF6FA8C7)
        )
        HeatmapAccent.Amber -> listOf(
            Color(0xFFECEFEA),
            Color(0xFFF2DEC1),
            Color(0xFFC99861)
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
            onClick = onAdd,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FitBoardColors.activeCardBg,
                contentColor = FitBoardColors.badgeActiveText,
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
    description: String,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, FitBoardColors.inactiveCardBorder, RoundedCornerShape(14.dp))
            .background(FitBoardColors.inactiveCardBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
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
            Text(
                text = description,
                fontSize = 11.sp,
                color = FitBoardColors.textHint
            )
        }
        Spacer(Modifier.width(8.dp))
        if (canDelete) {
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
    }
}

@Composable
private fun PlaceholderActionButton(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, FitBoardColors.cardBorder, RoundedCornerShape(14.dp))
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
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, FitBoardColors.innerPanelBorder, RoundedCornerShape(12.dp))
            .background(FitBoardColors.innerPanelBg)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(text, fontSize = 13.sp, color = FitBoardColors.textHint)
    }
}
