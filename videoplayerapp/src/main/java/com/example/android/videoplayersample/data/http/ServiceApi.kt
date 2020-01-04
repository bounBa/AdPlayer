package com.example.android.videoplayersample.data.http

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ServiceApi {
    @GET("adPlayer/list/{id}")
    fun getAdFileList(@Path("id") id : String) : Single<List<AdFile>>

    @POST("adPlayer/insert")
    fun upsertId(@Body body: AdPlayerId) : Single<ResultFlag>

    @Streaming
    @GET
    fun downloadFileWithDynamicUrlSync(@Url fileUrl: String) : Call<ResponseBody>

    @Streaming
    @GET("ads/ad1.mov")
    fun downloadFileWithFixedUrl(): Call<ResponseBody>




}