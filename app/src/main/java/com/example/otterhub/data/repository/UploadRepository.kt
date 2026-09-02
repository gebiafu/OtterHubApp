package com.example.otterhub.data.repository

import com.example.otterhub.data.api.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UploadRepository {

    suspend fun uploadSingleFile(
        file: File,
        nsfw: Boolean = false,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        return try {
            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val nsfwPart = nsfw.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = RetrofitClient.api.uploadFile(filePart, nsfwPart)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data?.key ?: "")
            } else {
                Result.Error(response.body()?.message ?: "上传失败")
            }
        } catch (e: Exception) {
            Result.Error("上传失败: ${e.localizedMessage}")
        }
    }

    suspend fun initChunkUpload(
        fileType: String,
        fileName: String,
        fileSize: Long,
        totalChunks: Int
    ): Result<String> {
        return try {
            val request = com.example.otterhub.data.model.ChunkUploadInitRequest(
                fileType = fileType,
                fileName = fileName,
                fileSize = fileSize,
                totalChunks = totalChunks
            )
            val response = RetrofitClient.api.initChunkUpload(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data ?: "")
            } else {
                Result.Error(response.body()?.message ?: "初始化上传失败")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }

    suspend fun uploadChunk(
        key: String,
        chunkIndex: Int,
        chunkFile: File
    ): Result<Int> {
        return try {
            val keyPart = key.toRequestBody("text/plain".toMediaTypeOrNull())
            val indexPart = chunkIndex.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val requestFile = chunkFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val chunkPart = MultipartBody.Part.createFormData("chunkFile", chunkFile.name, requestFile)

            val response = RetrofitClient.api.uploadChunk(keyPart, indexPart, chunkPart)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(chunkIndex)
            } else {
                Result.Error(response.body()?.message ?: "上传分片失败")
            }
        } catch (e: Exception) {
            Result.Error("上传分片失败: ${e.localizedMessage}")
        }
    }

    suspend fun getChunkProgress(key: String): Result<com.example.otterhub.data.model.ChunkUploadProgress> {
        return try {
            val response = RetrofitClient.api.getChunkProgress(key)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data!!)
            } else {
                Result.Error(response.body()?.message ?: "查询进度失败")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }
}
