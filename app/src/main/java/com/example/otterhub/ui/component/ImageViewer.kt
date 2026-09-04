package com.example.otterhub.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.otterhub.data.model.FileItem
import com.example.otterhub.util.FileUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewer(
    images: List<FileItem>,
    initialIndex: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // 确保索引有效
    val safeInitialIndex = initialIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0))
    
    // 使用 key 参数确保当 initialIndex 改变时重新创建 Pager
    key(images.size, safeInitialIndex) {
        val pagerState = rememberPagerState(
            initialPage = safeInitialIndex,
            pageCount = { images.size }
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (images.isEmpty()) {
                Text(
                    text = "没有图片",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { images[it].key }
                ) { page ->
                    val image = images[page]
                    ZoomableImage(
                        url = FileUtils.buildFileRawUrl(image.key),
                        contentDescription = image.fileName
                    )
                }

                // 页码指示器
                if (images.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${images.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // 为每个图片 URL 创建独立的状态
    key(url) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures(
                            panZoomLock = true
                        ) { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(0.5f, 5f)
                            
                            // 只有在进行缩放操作或已经缩放时才处理手势
                            if (zoom != 1f || scale > 1.01f) {
                                scale = newScale
                                
                                // 只有放大时才允许平移
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
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

        // 重置缩放
        LaunchedEffect(scale) {
            if (scale < 1f) {
                scale = 1f
                offsetX = 0f
                offsetY = 0f
            }
        }
    }
}
