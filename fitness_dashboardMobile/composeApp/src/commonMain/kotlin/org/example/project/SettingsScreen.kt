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
                text = "训练、补剂与本地数据说明",
                fontSize = 14.sp,
                color = FitBoardColors.textSecondary
            )
        }

        Spacer(Modifier.height(20.dp))

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

        Spacer(Modifier.height(12.dp))

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

        Spacer(Modifier.height(12.dp))

        LocalDataSection()

        Spacer(Modifier.height(12.dp))

        AboutSection()

        Spacer(Modifier.height(28.dp))
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
        AboutRow(key = "界面职责", value = "首页概览 / 记录录入 / 设置配置")
        Spacer(Modifier.height(8.dp))
        AboutRow(key = "技术实现", value = "Compose Multiplatform")
    }
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
