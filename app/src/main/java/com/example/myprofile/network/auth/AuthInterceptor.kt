package com.example.myprofile.network.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = Session.token()

        if (token.isNullOrBlank()) return chain.proceed(original)

        if (Jwt.isExpired(token)) {
            Session.clear()
            return chain.proceed(original)
        }

        val authed = original.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(authed)

        if (response.code == 401) {
            Session.clear()
        }
        return response
    }
}