package com.example.otterhub

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.example.otterhub.data.api.RetrofitClient

class OtterHubApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 让 Coil 复用 Retrofit 的 OkHttpClient，图片/缩略图请求会自动携带 auth Cookie。
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient(RetrofitClient.getOkHttpClient())
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)
    }
}
