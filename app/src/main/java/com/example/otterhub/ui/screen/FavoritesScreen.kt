package com.example.otterhub.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otterhub.data.model.FileItem
import com.example.otterhub.ui.component.EmptyState
import com.example.otterhub.ui.component.FileActionsMenu
import com.example.otterhub.ui.component.FileCard
import com.example.otterhub.ui.viewmodel.FileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onFileClick: (String) -> Unit,
    fileViewModel: FileViewModel = viewModel()
) {
    val uiState by fileViewModel.uiState.collectAsState()
    var menuFile by remember { mutableStateOf<FileItem?>(null) }

    LaunchedEffect(Unit) {
        fileViewModel.loadFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("收藏") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.files.isEmpty() && !uiState.isLoading) {
            EmptyState(
                message = "暂无收藏文件",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                items(uiState.files) { file ->
                    FileCard(
                        file = file,
                        onClick = { onFileClick(file.key) },
                        onLongClick = { menuFile = file },
                        onMoreClick = { menuFile = file }
                    )
                }
            }
        }

        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }

    // 文件上下文菜单
    FileActionsMenu(
        file = menuFile,
        onDismiss = { menuFile = null },
        onView = { key ->
            menuFile = null
            onFileClick(key)
        },
        onChanged = {
            fileViewModel.loadFavorites()
        }
    )
}
