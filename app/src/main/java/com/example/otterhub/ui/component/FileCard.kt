package com.example.otterhub.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.otterhub.data.model.FileItem
import com.example.otterhub.data.model.FileType
import com.example.otterhub.util.FileUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileCard(
    file: FileItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick?.invoke() }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Column {
                // Thumbnail or icon based on file type
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        file.fileType == FileType.IMAGE -> {
                            AsyncImage(
                                model = FileUtils.buildFileRawUrl(file.key),
                                contentDescription = file.fileName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        file.fileType == FileType.VIDEO -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (!file.metadata.thumbUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = file.metadata.thumbUrl,
                                        contentDescription = file.fileName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // 没有缩略图，显示图标
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "视频",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .align(Alignment.Center),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                // 播放图标叠加层
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "播放",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(Alignment.Center),
                                    tint = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        file.fileType == FileType.AUDIO -> {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "音频",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = "文件",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // File info
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = file.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = FileUtils.formatFileSize(file.fileSize),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // More actions button (context menu entry)
            if (onMoreClick != null) {
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多操作",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
