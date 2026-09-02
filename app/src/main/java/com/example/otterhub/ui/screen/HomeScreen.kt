package com.example.otterhub.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otterhub.data.model.FileType
import com.example.otterhub.ui.component.EmptyState
import com.example.otterhub.ui.component.FileCard
import com.example.otterhub.ui.component.FilterChips
import com.example.otterhub.ui.viewmodel.FileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFileClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onTrashClick: () -> Unit,
    onUploadClick: (Uri) -> Unit,
    fileViewModel: FileViewModel = viewModel()
) {
    val uiState by fileViewModel.uiState.collectAsState()
    var selectedType by remember { mutableStateOf<FileType?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUploadClick(it) }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            fileViewModel.clearError()
        }
    }

    LaunchedEffect(selectedType) {
        fileViewModel.loadFiles(fileType = selectedType, refresh = true)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                fileViewModel.searchFiles(it)
                            },
                            placeholder = { Text("搜索文件...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text("OtterHub")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) {
                            searchQuery = ""
                            fileViewModel.searchFiles("")
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Home else Icons.Default.Search,
                            contentDescription = if (isSearchActive) "关闭搜索" else "搜索"
                        )
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "收藏")
                    }
                    IconButton(onClick = onTrashClick) {
                        Icon(Icons.Default.Delete, contentDescription = "回收站")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { filePickerLauncher.launch("*/*") }) {
                Icon(Icons.Default.Add, contentDescription = "上传文件")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            FilterChips(
                selectedType = selectedType,
                onTypeSelected = { type ->
                    selectedType = type
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // File grid
            when {
                uiState.files.isEmpty() && !uiState.isLoading -> {
                    EmptyState(
                        message = if (selectedType != null) "该类型暂无文件" else "暂无文件，点击 + 上传"
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.files) { file ->
                            FileCard(
                                file = file,
                                onClick = { onFileClick(file.key) }
                            )
                        }
                    }
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Load more button
            if (uiState.hasMore && uiState.files.isNotEmpty() && !uiState.isLoading) {
                OutlinedButton(
                    onClick = { fileViewModel.loadFiles(fileType = selectedType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("加载更多")
                }
            }
        }
    }
}
