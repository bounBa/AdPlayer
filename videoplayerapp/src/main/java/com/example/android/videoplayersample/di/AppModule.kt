package com.example.android.videoplayersample.di

import com.example.android.videoplayersample.data.DataModel
import com.example.android.videoplayersample.data.http.ServiceApi
import org.koin.dsl.module.module
import retrofit2.Retrofit

val appModule = module {
    single(createOnStart = false) {get<Retrofit>().create(ServiceApi::class.java)}

    single { DataModel(get()) }

}