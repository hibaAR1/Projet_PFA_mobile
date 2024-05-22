package com.example.myapplication

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

object ImgBBApiClient {
    private const val BASE_URL = "https://api.imgbb.com/1/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val imgBBService: ImgBBService = retrofit.create(ImgBBService::class.java)
}

interface ImgBBService {
    @Multipart
    @POST("upload")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        @Query("key") apiKey: String
    ): Call<ImgBBResponse>
}

data class ImgBBResponse(
    val data: ImgBBData,
    val success: Boolean,
    val status: Int
)

data class ImgBBData(
    val url: String,
    val thumb: ImgBBThumb
)

data class ImgBBThumb(
    val url: String
)
