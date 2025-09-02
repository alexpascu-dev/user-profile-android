package com.example.myprofile.network

import android.content.Context
import com.example.myprofile.network.auth.AuthInterceptor
import com.example.myprofile.network.auth.Session
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.myprofile.BuildConfig

object RetrofitInstance {

    lateinit var api: ApiService
        private set

    private const val BASE_URL = BuildConfig.BASE_URL

    fun init(context: Context) {
        if (::api.isInitialized) return

        Session.init(context)

        val logging = HttpLoggingInterceptor { msg -> Log.d("HTTP", msg) }
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        api = retrofit.create(ApiService::class.java)
    }
}