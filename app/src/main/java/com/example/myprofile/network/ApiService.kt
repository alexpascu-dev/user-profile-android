package com.example.myprofile.network

import com.example.myprofile.models.LoginDto
import com.example.myprofile.models.LoginResponseDto
import com.example.myprofile.models.PrinterInfoDto
import com.example.myprofile.models.SavePrinterMacDto
import com.example.myprofile.models.UpdateUserDto
import com.example.myprofile.models.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/login")
    suspend fun loginUser(@Body body: LoginDto): LoginResponseDto

    @GET("api/users/me")
    suspend fun me(): User

    @GET("api/users/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): User

    @PUT("api/users/update")
    suspend fun editUser(@Body body: UpdateUserDto)

    @GET("api/printer/info")
    suspend fun getPrinterInfo(): PrinterInfoDto

    @POST("api/printer/mac")
    suspend fun savePrinterMac(@Body body: SavePrinterMacDto): Unit
}