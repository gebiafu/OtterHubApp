package com.example.otterhub.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.otterhub.data.model.FileType
import com.example.otterhub.ui.viewmodel.PreviewViewModel
import com.example.otterhub.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    fileKey: String,
    onBack: () -> Unit,
    onShare: (String) -> Unit = {},
    previewViewModel: PreviewViewModel = viewModel()
) {
    val uiState by previewViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(fileKey) {
        previewViewModel.loadFileInfo(fileKey)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.file?.fileName ?: "预览") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    uiState.file?.let { file ->
                        IconButton(onClick = { previewViewModel.toggleLike(fileKey) }) {
                            Icon(
                                imageVector = if (file.metadata.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (file.metadata.liked) "取消收藏" else "收藏",
                                tint = if (file.metadata.liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Info, contentDescription = "详情")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
                uiState.file != null -> {
                    when (uiState.file!!.fileType) {
                        FileType.IMAGE -> {
                            var scale by remember { mutableStateOf(1f) }
                            var offsetX by remember { mutableStateOf(0f) }
                            var offsetY by remember { mutableStateOf(0f) }

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(previewViewModel.getRawUrl(fileKey))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = uiState.file!!.fileName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                                            offsetX += pan.x
                                            offsetY += pan.y
                                        }
                                    }
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                    ),
                                contentScale = ContentScale.Fit
                            )
                        }
                        FileType.VIDEO -> {
                            // Video player placeholder - would use Media3 ExoPlayer
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = uiState.file!!.fileName,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "视频播放功能开发中...",
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        FileType.AUDIO -> {
                            // Audio player placeholder
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = uiState.file!!.fileName,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "音频播放功能开发中...",
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        else -> {
                            // Document preview placeholder
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = uiState.file!!.fileName,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "文件类型: ${uiState.file!!.fileType?.name ?: "未知"}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "大小: ${FileUtils.formatFileSize(uiState.file!!.fileSize)}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
