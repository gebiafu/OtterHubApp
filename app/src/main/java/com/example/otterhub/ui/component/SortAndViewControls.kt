package com.example.otterhub.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class SortOrder {
    UPLOAD_TIME_DESC,
    UPLOAD_TIME_ASC,
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC
}

enum class ViewMode {
    GRID,
    LIST
}

@Composable
fun SortAndViewControls(
    sortOrder: SortOrder,
    viewMode: ViewMode,
    onSortOrderChange: (SortOrder) -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 排序按钮
        var showSortMenu by remember { mutableStateOf(false) }
        Box {
            IconButton(onClick = { showSortMenu = true }) {
                Icon(Icons.Default.Sort, contentDescription = "排序")
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("上传时间 ↓") },
                    onClick = {
                        onSortOrderChange(SortOrder.UPLOAD_TIME_DESC)
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("上传时间 ↑") },
                    onClick = {
                        onSortOrderChange(SortOrder.UPLOAD_TIME_ASC)
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("文件名 A-Z") },
                    onClick = {
                        onSortOrderChange(SortOrder.NAME_ASC)
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("文件名 Z-A") },
                    onClick = {
                        onSortOrderChange(SortOrder.NAME_DESC)
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("大小 ↓") },
                    onClick = {
                        onSortOrderChange(SortOrder.SIZE_DESC)
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("大小 ↑") },
                    onClick = {
                        onSortOrderChange(SortOrder.SIZE_ASC)
                        showSortMenu = false
                    }
                )
            }
        }

        // 视图模式切换
        IconButton(onClick = {
            onViewModeChange(if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID)
        }) {
            Icon(
                imageVector = if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                contentDescription = if (viewMode == ViewMode.GRID) "列表视图" else "网格视图"
            )
        }
    }
}
