package com.example.otterhub.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.otterhub.data.model.FileType

@Composable
fun FilterChips(
    selectedType: FileType?,
    onTypeSelected: (FileType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text("全部") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        FilterChip(
            selected = selectedType == FileType.IMAGE,
            onClick = { onTypeSelected(FileType.IMAGE) },
            label = { Text("图片") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        FilterChip(
            selected = selectedType == FileType.VIDEO,
            onClick = { onTypeSelected(FileType.VIDEO) },
            label = { Text("视频") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        FilterChip(
            selected = selectedType == FileType.AUDIO,
            onClick = { onTypeSelected(FileType.AUDIO) },
            label = { Text("音频") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        FilterChip(
            selected = selectedType == FileType.DOCUMENT,
            onClick = { onTypeSelected(FileType.DOCUMENT) },
            label = { Text("文档") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
