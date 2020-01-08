package com.example.android.videoplayersample

import android.app.Application
import android.util.Log
import com.example.android.videoplayersample.di.appModule
import com.example.android.videoplayersample.di.httpModule
import fr.bipi.tressence.console.ThrowErrorTree
import org.koin.android.ext.android.startKoin
import timber.log.Timber
import fr.bipi.tressence.file.FileLoggerTree
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Environment
import java.io.File


class AdPlayer : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(
            appModule,
            httpModule
        ))

        var dir = File(Environment.getExternalStorageDirectory(), "Ads")
//        System.out.println("File Foond " + mFile2!!.absolutePath)

        val t = FileLoggerTree.Builder()
            .withFileName("file%g.log")
            .withDirName(dir!!.absolutePath)
            .withSizeLimit(20000)
            .withFileLimit(3)
            .withMinPriority(Log.DEBUG)
            .appendToFile(true)
            .build()
        Timber.plant(t)
    }
}