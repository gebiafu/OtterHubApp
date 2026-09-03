package com.example.otterhub.data.model

import com.google.gson.annotations.SerializedName

enum class FileType(val apiValue: String, val displayName: String) {
    @SerializedName("img") IMAGE("img", "图片"),
    @SerializedName("audio") AUDIO("audio", "音频"),
    @SerializedName("video") VIDEO("video", "视频"),
    @SerializedName("doc") DOCUMENT("doc", "文档"),
    @SerializedName("trash") TRASH("trash", "回收站")
}

enum class FileTag(val value: String) {
    @SerializedName("nsfw") NSFW("nsfw"),
    @SerializedName("private") PRIVATE("private")
}

data class FileMetadata(
    val fileName: String = "",
    val fileSize: Long = 0,
    val uploadedAt: String = "",
    val liked: Boolean = false,
    val tags: List<String>? = null,
    val thumbUrl: String? = null,
    val desc: String? = null,
    val chunkInfo: ChunkInfo? = null
)

data class ChunkInfo(
    val total: Int = 0,
    val uploadedIndices: List<Int> = emptyList()
)

data class FileItem(
    val name: String = "",
    val metadata: FileMetadata = FileMetadata(),
    val expiration: Long? = null
) {
    val key: String get() = name
    val fileName: String get() = metadata.fileName
    val fileSize: Long get() = metadata.fileSize
    val fileType: FileType?
        get() = when {
            name.startsWith("img:") -> FileType.IMAGE
            name.startsWith("audio:") -> FileType.AUDIO
            name.startsWith("video:") -> FileType.VIDEO
            name.startsWith("doc:") -> FileType.DOCUMENT
            else -> null
        }
}

data class ListFilesResponse(
    val keys: List<FileItem> = emptyList(),
    @SerializedName("list_complete") val listComplete: Boolean = true,
    val cursor: String? = null
)

data class ApiResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null
)

data class LoginRequest(val password: String)
data class LoginResponse(val success: Boolean, val token: String? = null)

data class ShareInfo(
    val token: String = "",
    val fileCount: Int = 0,
    val files: List<ShareFileItem> = emptyList(),
    val url: String = "",
    val createdAt: String = "",
    val expiresAt: String? = null
)

data class ShareFileItem(
    val name: String = "",
    val url: String? = null
)

data class ShareMetaResponse(
    val fileName: String = "",
    val fileSize: Long = 0,
    val mimeType: String? = null,
    val fileCount: Int? = null,
    val bundleName: String? = null,
    val expiry: String? = null
)

data class CreateShareRequest(
    val type: String = "single",
    @SerializedName("fileKey") val fileKey: String? = null,
    @SerializedName("fileKeys") val fileKeys: List<String>? = null,
    @SerializedName("bundleName") val bundleName: String? = null,
    @SerializedName("expireIn") val expireIn: Long? = null
)

data class ChunkUploadInitRequest(
    @SerializedName("fileType") val fileType: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fileSize") val fileSize: Long,
    @SerializedName("totalChunks") val totalChunks: Int,
    val tags: List<String>? = null
)

data class ChunkUploadProgress(
    @SerializedName("uploadedIndices") val uploadedIndices: List<Int> = emptyList(),
    val uploaded: Int = 0,
    val total: Int = 0,
    val complete: Boolean = false
)

data class UploadResult(
    val key: String = ""
)
