package com.example.otterhub.data.api

import com.example.otterhub.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OtterHubApi {

    // ==================== Auth ====================

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    // ==================== File ====================

    @GET("file/list")
    suspend fun getFileList(
        @Query("fileType") fileType: String? = null,
        @Query("limit") limit: Int = 30,
        @Query("cursor") cursor: String? = null
    ): Response<ApiResponse<ListFilesResponse>>

    @GET("file/{key}/thumb")
    suspend fun getFileThumb(@Path("key", encoded = true) key: String): Response<ResponseBody>

    @GET("file/{key}/download")
    suspend fun downloadFile(@Path("key", encoded = true) key: String): Response<ResponseBody>

    @GET("file/{key}")
    suspend fun getFileRaw(@Path("key", encoded = true) key: String): Response<ResponseBody>

    @PATCH("file/{key}/meta")
    suspend fun updateFileMeta(
        @Path("key", encoded = true) key: String,
        @Body body: Map<String, Any?>
    ): Response<ApiResponse<Unit>>

    @POST("file/{key}/toggle-like")
    suspend fun toggleLike(@Path("key", encoded = true) key: String): Response<ApiResponse<Unit>>

    @DELETE("file/{key}")
    suspend fun deleteFile(@Path("key", encoded = true) key: String): Response<ApiResponse<Unit>>

    // ==================== Upload ====================

    @Multipart
    @POST("upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("nsfw") nsfw: RequestBody,
        @Part("tags") tags: RequestBody? = null
    ): Response<ApiResponse<String>>

    @POST("upload/by-url")
    suspend fun uploadByUrl(@Body body: Map<String, Any?>): Response<ApiResponse<UploadResult>>

    @POST("upload/chunk/init")
    suspend fun initChunkUpload(@Body request: ChunkUploadInitRequest): Response<ApiResponse<String>>

    @Multipart
    @POST("upload/chunk")
    suspend fun uploadChunk(
        @Part("key") key: RequestBody,
        @Part("chunkIndex") chunkIndex: RequestBody,
        @Part chunkFile: MultipartBody.Part
    ): Response<ApiResponse<Int>>

    @GET("upload/chunk/progress")
    suspend fun getChunkProgress(@Query("key") key: String): Response<ApiResponse<ChunkUploadProgress>>

    // ==================== Share ====================

    @POST("share/create")
    suspend fun createShare(@Body request: CreateShareRequest): Response<ApiResponse<ShareInfo>>

    @GET("share/list")
    suspend fun getShareList(): Response<ApiResponse<List<ShareInfo>>>

    @DELETE("share/revoke/{token}")
    suspend fun revokeShare(@Path("token") token: String): Response<ApiResponse<Unit>>

    @GET("share/{token}/meta")
    suspend fun getShareMeta(@Path("token") token: String): Response<ApiResponse<ShareMetaResponse>>

    @GET("share/{token}/raw")
    suspend fun getShareRaw(
        @Path("token") token: String,
        @Query("file") fileKey: String? = null
    ): Response<ResponseBody>

    // ==================== Trash ====================

    @POST("trash/{key}/move")
    suspend fun moveToTrash(@Path("key", encoded = true) key: String): Response<ApiResponse<Unit>>

    @POST("trash/{key}/restore")
    suspend fun restoreFromTrash(@Path("key", encoded = true) key: String): Response<ApiResponse<Unit>>

    // ==================== Settings ====================

    @GET("settings/general")
    suspend fun getGeneralSettings(): Response<ApiResponse<Map<String, Any>>>

    @POST("settings/general")
    suspend fun updateGeneralSettings(@Body body: Map<String, Any?>): Response<ApiResponse<Unit>>

    // ==================== Health ====================

    @GET("health")
    suspend fun healthCheck(): Response<Map<String, Any>>
}
