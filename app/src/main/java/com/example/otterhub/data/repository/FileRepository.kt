package com.example.otterhub.data.repository

import com.example.otterhub.data.api.RetrofitClient
import com.example.otterhub.data.model.FileItem
import com.example.otterhub.data.model.FileType
import okhttp3.ResponseBody
import java.io.InputStream

class FileRepository {

    suspend fun getFileList(
        fileType: FileType? = null,
        limit: Int = 30,
        cursor: String? = null
    ): Result<Pair<List<FileItem>, String?>> {
        return try {
            val response = RetrofitClient.api.getFileList(
                fileType = fileType?.apiValue,
                limit = limit,
                cursor = cursor
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data!!
                Result.Success(data.keys to data.cursor)
            } else {
                Result.Error(response.body()?.message ?: "获取文件列表失败")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }

    suspend fun getFileThumb(key: String): Result<InputStream> {
        return try {
            val response = RetrofitClient.api.getFileThumb(key)
            if (response.isSuccessful) {
                Result.Success(response.body()!!.byteStream())
            } else {
                Result.Error("获取缩略图失败")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }

    suspend fun downloadFile(key: String): Result<InputStream> {
        return try {
            val response = RetrofitClient.api.downloadFile(key)
            if (response.isSuccessful) {
                Result.Success(response.body()!!.byteStream())
            } else {
                Result.Error("下载失败")
            }
        } catch (e: Exception) {
            Result.Error("下载失败: ${e.localizedMessage}")
        }
    }

    suspend fun getFileStream(key: String): Result<ResponseBody> {
        return try {
            val response = RetrofitClient.api.getFileRaw(key)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("获取文件失败")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }

    suspend fun toggleLike(key: String): Result<Unit> {
        return try {
            val response = RetrofitClient.api.toggleLike(key)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error(response.body()?.message ?: "操作失败")
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }

    suspend fun deleteFile(key: String): Result<Unit> {
        return try {
            val response = RetrofitClient.api.deleteFile(key)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "删除失败"
                Result.Error("删除失败: $errorMsg (HTTP ${response.code()})")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.message}")
        }
    }

    suspend fun moveToTrash(key: String): Result<Unit> {
        return try {
            val response = RetrofitClient.api.moveToTrash(key)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "移入回收站失败"
                Result.Error("移入回收站失败: $errorMsg (HTTP ${response.code()})")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.message}")
        }
    }

    suspend fun restoreFromTrash(key: String): Result<Unit> {
        return try {
            val response = RetrofitClient.api.restoreFromTrash(key)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "恢复失败"
                Result.Error("恢复失败: $errorMsg (HTTP ${response.code()})")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.message}")
        }
    }

    suspend fun updateMeta(key: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            val response = RetrofitClient.api.updateFileMeta(key, updates)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error(response.body()?.message ?: "更新失败")
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }

    fun getFileUrl(key: String): String {
        return "${RetrofitClient.getBaseUrl()}/file/${java.net.URLEncoder.encode(key, "UTF-8")}"
    }

    fun getFileThumbUrl(key: String): String {
        return "${RetrofitClient.getBaseUrl()}/file/${java.net.URLEncoder.encode(key, "UTF-8")}/thumb"
    }

    fun getFileDownloadUrl(key: String): String {
        return "${RetrofitClient.getBaseUrl()}/file/${java.net.URLEncoder.encode(key, "UTF-8")}/download"
    }
}
