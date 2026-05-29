package com.autovolume.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileNames: List<String>,
    currentProfileName: String,
    onBack: () -> Unit,
    onCreateProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onRenameProfile: (String, String) -> Unit,
    onSwitchProfile: (String) -> Unit,
    onExportProfile: (String, (String?) -> Unit) -> Unit,
    onImportProfile: (String, (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var exportResult by remember { mutableStateOf<String?>(null) }
    var importResult by remember { mutableStateOf<String?>(null) }

    // SAF 文件选择器：导出
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { fileUri ->
            exportResult?.let { json ->
                context.contentResolver.openOutputStream(fileUri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                exportResult = null
            }
        }
    }

    // SAF 文件选择器：导入
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { fileUri ->
            val json = context.contentResolver.openInputStream(fileUri)?.use { stream ->
                stream.bufferedReader().readText()
            }
            if (json != null) {
                onImportProfile(json) { success ->
                    importResult = if (success) "导入成功" else "导入失败：配置格式错误"
                }
            }
        }
    }

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
                    // 导入按钮
                    IconButton(onClick = {
                        importLauncher.launch(arrayOf("application/json"))
                    }) {
                        Icon(Icons.Default.Share, "导入配置")
                    }
                    // 新建按钮
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
            // 说明
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("配置说明", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "每个配置独立保存灵敏度、音量范围、映射曲线、响应速度等参数。" +
                                    "创建配置时会保存当前设置。支持导出为 JSON 文件。",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 默认配置（不可删除，不可重命名）
            item {
                ProfileCard(
                    name = "默认",
                    isCurrent = currentProfileName == "默认",
                    canDelete = false,
                    canRename = false,
                    onSwitch = { onSwitchProfile("默认") },
                    onRename = null,
                    onDelete = null,
                    onExport = {
                        onExportProfile("默认") { json ->
                            if (json != null) {
                                exportResult = json
                                exportLauncher.launch("autovolume_default.json")
                            }
                        }
                    }
                )
            }

            // 用户自定义配置
            items(profileNames.filter { it != "默认" }) { name ->
                ProfileCard(
                    name = name,
                    isCurrent = currentProfileName == name,
                    canDelete = true,
                    canRename = true,
                    onSwitch = { onSwitchProfile(name) },
                    onRename = { showRenameDialog = name },
                    onDelete = { showDeleteDialog = name },
                    onExport = {
                        onExportProfile(name) { json ->
                            if (json != null) {
                                exportResult = json
                                exportLauncher.launch("autovolume_${name}.json")
                            }
                        }
                    }
                )
            }

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

    // 导入结果提示
    importResult?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000)
            importResult = null
        }
        Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
    }

    // 新建配置对话框
    if (showCreateDialog) {
        ProfileNameDialog(
            title = "新建配置",
            initialName = "",
            onConfirm = { name -> onCreateProfile(name); showCreateDialog = false },
            onDismiss = { showCreateDialog = false }
        )
    }

    // 重命名对话框
    showRenameDialog?.let { oldName ->
        ProfileNameDialog(
            title = "重命名配置",
            initialName = oldName,
            onConfirm = { newName -> onRenameProfile(oldName, newName); showRenameDialog = null },
            onDismiss = { showRenameDialog = null }
        )
    }

    // 删除确认对话框
    showDeleteDialog?.let { name ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除配置") },
            text = { Text("确定要删除配置「$name」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = { onDeleteProfile(name); showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun ProfileCard(
    name: String,
    isCurrent: Boolean,
    canDelete: Boolean,
    canRename: Boolean,
    onSwitch: () -> Unit,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onExport: () -> Unit
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
                        Icons.Default.CheckCircle, null,
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
                        Text("当前使用中", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Row {
                // 导出
                IconButton(onClick = onExport, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Share, "导出", modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (canRename && onRename != null) {
                    IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, "重命名", modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (canDelete && onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

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
                onValueChange = { name = it; isError = it.isBlank() || it == "默认" },
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
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
