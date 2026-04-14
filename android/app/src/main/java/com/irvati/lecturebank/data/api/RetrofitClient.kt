package com.irvati.lecturebank.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Singleton-клиент Retrofit для работы с бэкендом
object RetrofitClient {

    // Адрес бэкенда: по умолчанию Android-эмулятор (10.0.2.2 — localhost хост-машины).
    // Для физических устройств или других сред измените значение через BuildConfig или конфиг-файл.
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Логирование HTTP-запросов и ответов
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Единственный экземпляр ApiService
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
