package com.example.android.videoplayersample.data

import com.example.android.videoplayersample.data.http.AdFile
import com.example.android.videoplayersample.data.http.ServiceApi

class DataModel(val httpApi: ServiceApi) {
    val mockupAdFileList = listOf(
            AdFile( "1.mov")
    )

}