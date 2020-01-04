package com.example.android.videoplayersample

import android.app.Application
import com.example.android.videoplayersample.di.appModule
import com.example.android.videoplayersample.di.httpModule
import org.koin.android.ext.android.startKoin

class AdPlayer : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(
            appModule,
            httpModule
        ))
    }
}