package com.example.myprofile.models

import com.squareup.moshi.Json

data class UpdateUserDto (
    val userId: Int,
    @Json(name = "username")
    val userName: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean
) {
    val name: String
        get() = "$firstName $lastName"
}