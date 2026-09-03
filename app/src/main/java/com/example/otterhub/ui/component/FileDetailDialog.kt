package com.example.otterhub.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.otterhub.data.model.FileItem
import com.example.otterhub.util.FileUtils

@Composable
fun FileDetailDialog(
    file: FileItem?,
    onDismiss: () -> Unit
) {
    if (file == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("文件详情") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailItem("文件名", file.fileName)
                DetailItem("大小", FileUtils.formatFileSize(file.fileSize))
                DetailItem("类型", file.fileType?.displayName ?: "未知")
                DetailItem("上传时间", FileUtils.formatUploadDate(file.metadata.uploadedAt))
                
                if (!file.metadata.tags.isNullOrEmpty()) {
                    DetailItem("标签", file.metadata.tags!!.joinToString(", "))
                }
                
                if (!file.metadata.desc.isNullOrBlank()) {
                    DetailItem("描述", file.metadata.desc!!)
                }
                
                if (file.metadata.thumbUrl != null) {
                    DetailItem("缩略图", "有")
                }
                
                DetailItem("文件键", file.key)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
