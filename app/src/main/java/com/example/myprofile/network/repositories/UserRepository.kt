package com.example.myprofile.network.repositories

import com.example.myprofile.models.User
import com.example.myprofile.network.ApiService

class UserRepository(private val api: ApiService) {
    suspend fun getUser(username: String): User = api.getUserByUsername(username)
}
