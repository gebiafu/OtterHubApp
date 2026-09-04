package com.example.otterhub.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otterhub.data.model.FileItem
import com.example.otterhub.data.model.FileType
import com.example.otterhub.ui.component.EmptyState
import com.example.otterhub.ui.component.FileActionsMenu
import com.example.otterhub.ui.component.FileCard
import com.example.otterhub.ui.component.FileListItem
import com.example.otterhub.ui.component.FilterChips
import com.example.otterhub.ui.component.SortOrder
import com.example.otterhub.ui.component.UploadProgress
import com.example.otterhub.ui.component.ViewMode
import com.example.otterhub.ui.viewmodel.FileViewModel
import com.example.otterhub.ui.viewmodel.UploadViewModel
import com.example.otterhub.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFileClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onTrashClick: () -> Unit,
    onLogout: () -> Unit,
    sortOrder: SortOrder,
    viewMode: ViewMode,
    fileViewModel: FileViewModel = viewModel(),
    uploadViewModel: UploadViewModel = viewModel()
) {
    val uiState by fileViewModel.uiState.collectAsState()
    val uploadState by uploadViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf<FileType?>(FileType.IMAGE) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var menuFile by remember { mutableStateOf<FileItem?>(null) }
    
    // 首次加载标记
    var hasInitialLoaded by remember { mutableStateOf(false) }

    val sortedFiles = remember(uiState.files, sortOrder) {
        when (sortOrder) {
            SortOrder.UPLOAD_TIME_DESC -> uiState.files.sortedByDescending { it.metadata.uploadedAt }
            SortOrder.UPLOAD_TIME_ASC -> uiState.files.sortedBy { it.metadata.uploadedAt }
            SortOrder.NAME_ASC -> uiState.files.sortedBy { it.fileName.lowercase() }
            SortOrder.NAME_DESC -> uiState.files.sortedByDescending { it.fileName.lowercase() }
            SortOrder.SIZE_DESC -> uiState.files.sortedByDescending { it.fileSize }
            SortOrder.SIZE_ASC -> uiState.files.sortedBy { it.fileSize }
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val name = FileUtils.resolveFileName(context, it)
            uploadViewModel.uploadFile(it, name)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            fileViewModel.clearError()
        }
    }

    // 首次加载和类型切换时刷新
    LaunchedEffect(selectedType) {
        if (!hasInitialLoaded || selectedType != null) {
            fileViewModel.loadFiles(fileType = selectedType, refresh = false)
            hasInitialLoaded = true
        }
    }

    LaunchedEffect(uploadState.success, uploadState.error) {
        when {
            uploadState.success -> {
                snackbarHostState.showSnackbar("上传成功")
                uploadViewModel.resetState()
                fileViewModel.loadFiles(fileType = selectedType, refresh = true)
            }
            uploadState.error != null -> {
                snackbarHostState.showSnackbar(uploadState.error!!)
                uploadViewModel.clearError()
            }
        }
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
                    IconButton(onClick = { fileViewModel.loadFiles(fileType = selectedType, refresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
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
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("*/*") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "上传文件")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
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

            // File grid or list
            when {
                uiState.files.isEmpty() && !uiState.isLoading -> {
                    EmptyState(
                        message = "该类型暂无文件，点击右下角 + 上传"
                    )
                }
                viewMode == ViewMode.GRID -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sortedFiles) { file ->
                            FileCard(
                                file = file,
                                onClick = { onFileClick(file.key) },
                                onLongClick = { menuFile = file },
                                onMoreClick = { menuFile = file }
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortedFiles) { file ->
                            FileListItem(
                                file = file,
                                onClick = { onFileClick(file.key) },
                                onMoreClick = { menuFile = file }
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

            if (uploadState.isUploading) {
                UploadProgress(
                    fileName = uploadState.currentFileName,
                    progress = uploadState.progress,
                    isChunked = uploadState.isChunked,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
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
            // 手动操作后刷新列表
            fileViewModel.loadFiles(fileType = selectedType, refresh = true)
        }
    )
}
