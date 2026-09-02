package com.example.otterhub.util

import com.example.otterhub.data.model.FileType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val safeIndex = digitGroups.coerceAtMost(units.size - 1)
        return String.format(
            Locale.US, "%.1f %s",
            bytes / Math.pow(1024.0, safeIndex.toDouble()),
            units[safeIndex]
        )
    }

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatUploadDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
            val date = inputFormat.parse(dateStr)
            if (date != null) outputFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr.take(16)
        }
    }

    fun getFileTypeFromKey(key: String): FileType? {
        return when {
            key.startsWith("img:") -> FileType.IMAGE
            key.startsWith("audio:") -> FileType.AUDIO
            key.startsWith("video:") -> FileType.VIDEO
            key.startsWith("doc:") -> FileType.DOCUMENT
            else -> null
        }
    }

    fun getFileTypeForUpload(mimeType: String): FileType {
        return when {
            mimeType.startsWith("image/") -> FileType.IMAGE
            mimeType.startsWith("audio/") -> FileType.AUDIO
            mimeType.startsWith("video/") -> FileType.VIDEO
            else -> FileType.DOCUMENT
        }
    }

    fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "txt" -> "text/plain"
            "pdf" -> "application/pdf"
            "json" -> "application/json"
            else -> "application/octet-stream"
        }
    }

    fun isImageFile(key: String): Boolean = key.startsWith("img:")
    fun isVideoFile(key: String): Boolean = key.startsWith("video:")
    fun isAudioFile(key: String): Boolean = key.startsWith("audio:")
    fun isDocumentFile(key: String): Boolean = key.startsWith("doc:")
}
