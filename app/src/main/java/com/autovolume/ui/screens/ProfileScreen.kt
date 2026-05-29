package com.autovolume.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 配置管理页面
 *
 * 支持创建、删除、重命名、切换配置。
 * 例如：通勤模式、夜间模式、地铁模式、室内模式。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileNames: List<String>,
    currentProfileName: String,
    onBack: () -> Unit,
    onCreateProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onRenameProfile: (String, String) -> Unit,
    onSwitchProfile: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("配置管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "新建配置")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== 使用说明 =====
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "配置说明",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "每个配置独立保存灵敏度、音量范围、映射曲线、响应速度等参数。" +
                                    "创建配置时会保存当前设置。",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // ===== 默认配置（不可删除） =====
            item {
                ProfileCard(
                    name = "默认",
                    isCurrent = currentProfileName == "默认",
                    canDelete = false,
                    onSwitch = { onSwitchProfile("默认") },
                    onRename = null,
                    onDelete = null
                )
            }

            // ===== 用户自定义配置 =====
            items(profileNames.filter { it != "默认" }) { name ->
                ProfileCard(
                    name = name,
                    isCurrent = currentProfileName == name,
                    canDelete = true,
                    onSwitch = { onSwitchProfile(name) },
                    onRename = { showRenameDialog = name },
                    onDelete = { showDeleteDialog = name }
                )
            }

            // ===== 空状态提示 =====
            if (profileNames.none { it != "默认" }) {
                item {
                    Text(
                        "暂无自定义配置，点击右上角 + 创建新配置",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // ===== 新建配置对话框 =====
    if (showCreateDialog) {
        ProfileNameDialog(
            title = "新建配置",
            initialName = "",
            onConfirm = { name ->
                onCreateProfile(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // ===== 重命名对话框 =====
    showRenameDialog?.let { oldName ->
        ProfileNameDialog(
            title = "重命名配置",
            initialName = oldName,
            onConfirm = { newName ->
                onRenameProfile(oldName, newName)
                showRenameDialog = null
            },
            onDismiss = { showRenameDialog = null }
        )
    }

    // ===== 删除确认对话框 =====
    showDeleteDialog?.let { name ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除配置") },
            text = { Text("确定要删除配置「$name」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(name)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 配置卡片组件
 */
@Composable
private fun ProfileCard(
    name: String,
    isCurrent: Boolean,
    canDelete: Boolean,
    onSwitch: () -> Unit,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        onClick = { if (!isCurrent) onSwitch() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isCurrent) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Column {
                    Text(
                        name,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrent) {
                        Text(
                            "当前使用中",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row {
                if (onRename != null) {
                    IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            "重命名",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (canDelete && onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            "删除",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * 配置名称输入对话框
 */
@Composable
private fun ProfileNameDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    isError = it.isBlank() || it == "默认"
                },
                label = { Text("配置名称") },
                placeholder = { Text("例如：通勤模式") },
                isError = isError,
                supportingText = when {
                    name.isBlank() -> {{ Text("名称不能为空") }}
                    name == "默认" -> {{ Text("「默认」为系统保留名称") }}
                    else -> null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank() && name != "默认" && name.trim() == name
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
