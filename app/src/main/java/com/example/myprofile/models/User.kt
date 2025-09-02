package com.example.myprofile.models

import com.squareup.moshi.Json
import java.text.SimpleDateFormat

data class User (
    val userId: Int,
    val firstName: String,
    val lastName: String,
    @Json(name = "username")
    val userName: String,
    val email: String,
    val role: String,
    val createdDate: String,
    val isActive: Boolean
) {
    val name: String
        get() = "$firstName $lastName"

    val year: String
        get() = formatCreatedDate("yyyy")

    val monthAndDay: String
        get() = formatCreatedDate("MMMM d")

    private fun formatCreatedDate(pattern: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
            val date = inputFormat.parse(createdDate)
            val outputFormat = SimpleDateFormat(pattern)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            ""
        }
    }
}