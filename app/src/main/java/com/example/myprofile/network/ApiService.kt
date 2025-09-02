package com.example.myprofile.network

import com.example.myprofile.models.LoginDto
import com.example.myprofile.models.LoginResponseDto
import com.example.myprofile.models.UpdateUserDto
import com.example.myprofile.models.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {

    @POST("api/auth/login")
    suspend fun loginUser(@Body body: LoginDto): LoginResponseDto

    @GET("api/users/me")
    suspend fun me(): User

    @PUT("api/users/update")
    suspend fun editUser(@Body body: UpdateUserDto)
}