package com.example.AquaBalance

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("your-endpoint")  // Replace "your-endpoint" with the actual endpoint
    fun getData(): Call<ResponseBody>
}
