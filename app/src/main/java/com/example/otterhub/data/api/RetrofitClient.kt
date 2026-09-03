package com.example.otterhub.data.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var baseUrl: String = ""
    private var authToken: String? = null

    private val cookieStore = mutableMapOf<String, List<Cookie>>()

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val saved = cookieStore[url.host]?.toMutableList() ?: mutableListOf()
            authToken?.let { token ->
                val authCookie = Cookie.Builder()
                    .domain(url.host)
                    .path("/")
                    .name("auth")
                    .value(token)
                    .build()
                saved.removeAll { it.name == "auth" }
                saved.add(authCookie)
            }
            return saved
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder().apply {
            authToken?.let { addHeader("Cookie", "auth=$it") }
        }.build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit? = null

    fun configure(baseUrl: String, token: String? = null) {
        this.baseUrl = baseUrl.trimEnd('/')
        this.authToken = token
        retrofit = Retrofit.Builder()
            .baseUrl("${this.baseUrl}/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun setToken(token: String?) {
        authToken = token
    }

    fun getAuthToken(): String? = authToken

    fun getBaseUrl(): String = baseUrl

    /** 供 Coil / DownloadManager 等复用同一个 OkHttpClient，从而携带 auth Cookie。 */
    fun getOkHttpClient(): OkHttpClient = okHttpClient

    val api: OtterHubApi
        get() = retrofit?.create(OtterHubApi::class.java)
            ?: throw IllegalStateException("RetrofitClient not configured. Call configure() first.")
}
