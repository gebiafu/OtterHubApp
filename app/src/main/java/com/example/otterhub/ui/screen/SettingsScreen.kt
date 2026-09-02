package com.example.otterhub.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Server
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otterhub.data.local.PrefsManager
import com.example.otterhub.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    val baseUrl by prefs.baseUrl.collectAsState(initial = "")
    val scope = rememberCoroutineScope()
    var showSaveDialog by remember { mutableStateOf(false) }
    var newBaseUrl by remember { mutableStateOf("") }

    LaunchedEffect(baseUrl) {
        newBaseUrl = baseUrl
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Server URL section
            Text(
                text = "网盘地址",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newBaseUrl,
                onValueChange = { newBaseUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("服务器地址") },
                leadingIcon = { Icon(Icons.Default.Server, contentDescription = null) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showSaveDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存")
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(32.dp))

            // Logout section
            OutlinedButton(
                onClick = {
                    settingsViewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("退出登录")
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("修改网盘地址") },
            text = { Text("修改地址后需要重新登录，确定继续吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            prefs.saveSetupConfig(newBaseUrl, prefs.password.first())
                            settingsViewModel.resetSetup()
                            onLogout()
                        }
                        showSaveDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
