package com.example.otterhub.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.otterhub.data.model.FileType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedType: FileType?,
    onTypeSelected: (FileType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text("全部") }
        )
        
        FilterChip(
            selected = selectedType == FileType.IMAGE,
            onClick = { onTypeSelected(FileType.IMAGE) },
            label = { Text("图片") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        
        FilterChip(
            selected = selectedType == FileType.VIDEO,
            onClick = { onTypeSelected(FileType.VIDEO) },
            label = { Text("视频") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        
        FilterChip(
            selected = selectedType == FileType.AUDIO,
            onClick = { onTypeSelected(FileType.AUDIO) },
            label = { Text("音频") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AudioFile,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        
        FilterChip(
            selected = selectedType == FileType.DOCUMENT,
            onClick = { onTypeSelected(FileType.DOCUMENT) },
            label = { Text("文档") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}
