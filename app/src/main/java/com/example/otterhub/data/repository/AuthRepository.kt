package com.example.otterhub.data.repository

import com.example.otterhub.data.api.RetrofitClient
import com.example.otterhub.data.model.LoginRequest
import com.example.otterhub.data.model.ApiResponse
import com.example.otterhub.data.model.LoginResponse

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class AuthRepository {

    suspend fun login(password: String): Result<String> {
        return try {
            val response = RetrofitClient.api.login(LoginRequest(password))
            if (response.isSuccessful && response.body()?.success == true) {
                val token = response.body()?.data?.token
                if (token != null) {
                    Result.Success(token)
                } else {
                    Result.Error("登录响应中未包含 Token")
                }
            } else {
                Result.Error(response.body()?.message ?: "登录失败")
            }
        } catch (e: Exception) {
            Result.Error("网络错误: ${e.localizedMessage}")
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            RetrofitClient.api.logout()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("退出登录失败: ${e.localizedMessage}")
        }
    }

    suspend fun healthCheck(baseUrl: String): Result<Boolean> {
        return try {
            val response = RetrofitClient.api.healthCheck()
            if (response.isSuccessful) Result.Success(true)
            else Result.Error("服务器不可用")
        } catch (e: Exception) {
            Result.Error("无法连接服务器: ${e.localizedMessage}")
        }
    }
}
