package com.example.myprofile.network.auth

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object Jwt {

    data class Payload(
        val sub: String? = null,
        val unique_name: String? = null,
        val email: String? = null,
        val role: String? = null,
        val exp: Long? = null
    )

    fun isExpired(token: String, leewaySeconds: Int = 10): Boolean {
        val exp = decode(token).exp ?: return true
        val now = System.currentTimeMillis() / 1000
        return now >= (exp - leewaySeconds)
    }

    fun decode(token: String): Payload {
        val parts = token.split(".")
        if (parts.size < 2) return Payload()
        val payloadJson = decodeBase64UrlToJson(parts[1])
        return Payload(
            sub = payloadJson.optString("sub", null),
            unique_name = payloadJson.optString("unique_name", null),
            email = payloadJson.optString("email", null),
            role = payloadJson.optString("role", null),
            exp = payloadJson.optLong("exp", 0)
        )
    }

    private fun decodeBase64UrlToJson(base64Url: String): JSONObject {
        val bytes = Base64.decode(base64Url, Base64.DEFAULT)
//        val bytes = Base64.decode(base64Url, Base64.URL_SAFE or Base64.NO_WRAP)
        val json = String(bytes, StandardCharsets.UTF_8)
        return JSONObject(json)
    }
}