package com.example.otterhub.data.repository

import com.example.otterhub.data.api.RetrofitClient
import com.example.otterhub.data.model.CreateShareRequest
import com.example.otterhub.data.model.ShareInfo

class ShareRepository {

    suspend fun createShare(
        fileKey: String? = null,
        fileKeys: List<String>? = null,
        bundleName: String? = null,
        expireIn: Long? = null
    ): Result<String> {
        return try {
            val request = CreateShareRequest(
                type = if (fileKeys != null) "bundle" else "single",
                fileKey = fileKey,
                fileKeys = fileKeys,
                bundleName = bundleName,
                expireIn = expireIn
            )
            val response = RetrofitClient.api.createShare(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data?.token ?: "")
            } else {
                Result.Error(response.body()?.message ?: "创建分享失败")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }

    suspend fun getShareList(): Result<List<ShareInfo>> {
        return try {
            val response = RetrofitClient.api.getShareList()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data ?: emptyList())
            } else {
                Result.Error(response.body()?.message ?: "获取分享列表失败")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }

    suspend fun revokeShare(token: String): Result<Unit> {
        return try {
            val response = RetrofitClient.api.revokeShare(token)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error(response.body()?.message ?: "撤销分享失败")
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }

    fun getShareUrl(token: String): String {
        return "${RetrofitClient.getBaseUrl()}/s?k=$token"
    }
}
