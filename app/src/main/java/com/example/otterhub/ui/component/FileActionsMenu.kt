package com.example.otterhub.ui.component

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.otterhub.data.api.RetrofitClient
import com.example.otterhub.data.model.FileItem
import com.example.otterhub.data.repository.FileRepository
import com.example.otterhub.data.repository.Result
import com.example.otterhub.data.repository.ShareRepository
import com.example.otterhub.util.FileUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileActionsMenu(
    file: FileItem?,
    onDismiss: () -> Unit,
    onView: (String) -> Unit,
    onChanged: () -> Unit
) {
    if (file == null) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fileRepo = remember { FileRepository() }
    val shareRepo = remember { ShareRepository() }

    var showDetail by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showShareResult by remember { mutableStateOf(false) }
    var shareUrl by remember { mutableStateOf("") }
    var actionError by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("OtterHub", text))
    }

    fun downloadFile() {
        val url = FileUtils.buildFileDownloadUrl(file.key)
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(file.fileName)
            .setDescription("OtterHub 下载")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.fileName)
        RetrofitClient.getAuthToken()?.let { token ->
            request.addRequestHeader("Cookie", "auth=$token")
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }

    fun createShare() {
        scope.launch {
            when (val res = shareRepo.createShare(fileKey = file.key)) {
                is Result.Success -> {
                    shareUrl = shareRepo.getShareUrl(res.data)
                    showShareResult = true
                }
                is Result.Error -> actionError = res.message
            }
        }
    }

    fun deleteFile() {
        scope.launch {
            // 立即关闭对话框并触发刷新（乐观更新）
            onDismiss()
            onChanged()
            
            // 后台执行 API 调用
            fileRepo.moveToTrash(file.key)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = file.fileName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                maxLines = 1
            )

            ActionRow(Icons.Default.Visibility, "查看") { onView(file.key) }
            ActionRow(Icons.Default.Share, "分享") { createShare() }
            ActionRow(Icons.Default.Link, "复制链接") {
                copyToClipboard(FileUtils.buildFileRawUrl(file.key))
                onDismiss()
            }
            ActionRow(Icons.Default.Edit, "编辑") { showEdit = true }
            ActionRow(Icons.Default.Download, "下载") { downloadFile(); onDismiss() }
            ActionRow(Icons.Default.Info, "详情") { showDetail = true }
            ActionRow(Icons.Default.Delete, "删除", isDestructive = true) { showDeleteConfirm = true }
        }
    }

    // 详情
    if (showDetail) {
        AlertDialog(
            onDismissRequest = { showDetail = false },
            title = { Text("文件详情") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("文件名", file.fileName)
                    DetailRow("大小", FileUtils.formatFileSize(file.fileSize))
                    DetailRow("类型", file.fileType?.displayName ?: "未知")
                    DetailRow("上传时间", FileUtils.formatUploadDate(file.metadata.uploadedAt))
                    if (!file.metadata.tags.isNullOrEmpty()) {
                        DetailRow("标签", file.metadata.tags!!.joinToString(", "))
                    }
                    if (!file.metadata.desc.isNullOrBlank()) {
                        DetailRow("描述", file.metadata.desc!!)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetail = false }) { Text("关闭") }
            }
        )
    }

    // 编辑
    if (showEdit) {
        var newName by remember { mutableStateOf(file.fileName) }
        var newDesc by remember { mutableStateOf(file.metadata.desc ?: "") }
        var editing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEdit = false },
            title = { Text("编辑文件") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("文件名") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("描述") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !editing && newName.isNotBlank(),
                    onClick = {
                        editing = true
                        scope.launch {
                            val updates = buildMap<String, Any?> {
                                if (newName.isNotBlank()) put("fileName", newName)
                                if (newDesc.isNotBlank()) put("desc", newDesc)
                            }
                            when (val res = fileRepo.updateMeta(file.key, updates)) {
                                is Result.Success -> {
                                    showEdit = false
                                    onDismiss()
                                    onChanged()
                                }
                                is Result.Error -> actionError = res.message
                            }
                            editing = false
                        }
                    }
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showEdit = false }) { Text("取消") }
            }
        )
    }

    // 删除确认
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除文件") },
            text = { Text("确定要将「${file.fileName}」移入回收站吗？") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; deleteFile() }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    // 分享结果
    if (showShareResult) {
        AlertDialog(
            onDismissRequest = { showShareResult = false },
            title = { Text("分享链接已创建") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = shareUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    copyToClipboard(shareUrl)
                    showShareResult = false
                    onDismiss()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制链接")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareResult = false }) { Text("关闭") }
            }
        )
    }

    // 错误提示
    actionError?.let { msg ->
        AlertDialog(
            onDismissRequest = { actionError = null },
            title = { Text("操作失败") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { actionError = null }) { Text("知道了") }
            }
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
