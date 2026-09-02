package com.example.otterhub.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.otterhub.data.model.ShareInfo
import com.example.otterhub.data.repository.Result
import com.example.otterhub.data.repository.ShareRepository
import com.example.otterhub.ui.component.EmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shareRepo = ShareRepository()
    var shares by remember { mutableStateOf<List<ShareInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        when (val result = shareRepo.getShareList()) {
            is Result.Success -> {
                shares = result.data
                isLoading = false
            }
            is Result.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar("加载失败")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分享管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (shares.isEmpty() && !isLoading) {
            EmptyState(
                message = "暂无分享链接",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                items(shares) { share ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (share.files.isNotEmpty()) {
                                    share.files.joinToString(", ") { it.name }
                                } else {
                                    "${share.fileCount} 个文件"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (share.createdAt.isNotEmpty()) {
                                Text(
                                    text = "创建于 ${share.createdAt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (share.expiresAt != null) {
                                Text(
                                    text = "过期时间: ${share.expiresAt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("share link", share.url)
                                    clipboard.setPrimaryClip(clip)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("链接已复制")
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "复制链接"
                                    )
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        when (shareRepo.revokeShare(share.token)) {
                                            is Result.Success -> {
                                                shares = shares.filter { it.token != share.token }
                                                snackbarHostState.showSnackbar("已撤销")
                                            }
                                            is Result.Error -> {
                                                snackbarHostState.showSnackbar("撤销失败")
                                            }
                                        }
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "撤销分享",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}
