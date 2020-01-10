/*
 * Copyright 2018 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.videoplayersample

import android.annotation.TargetApi
import android.app.DownloadManager
import android.app.PictureInPictureParams
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.util.Rational
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.downloader.*
import com.example.android.videoplayersample.data.DataModel
import com.example.android.videoplayersample.data.http.AdFile
import com.example.android.videoplayersample.data.http.AdPlayerId
import com.example.android.videoplayersample.data.http.ResultFlag
import com.example.android.videoplayersample.download.DownloadManagerHelper
import com.example.android.videoplayersample.extensions.with
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.leopold.mvvm.util.NotNullMutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_video.*
import okhttp3.ResponseBody
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.*
import java.util.jar.Manifest
import kotlin.Error
import kotlin.concurrent.fixedRateTimer

/**
 * Allows playback of videos that are in a playlist, using [PlayerHolder] to load the and render
 * it to the [com.google.android.exoplayer2.ui.PlayerView] to render the video output. Supports
 * [MediaSessionCompat] and picture in picture as well.
 */

class VideoActivity : AppCompatActivity(), AnkoLogger {
    private val mediaSession: MediaSessionCompat by lazy { createMediaSession() }
    private val mediaSessionConnector: MediaSessionConnector by lazy {
        createMediaSessionConnector()
    }
    private val playerState by lazy { PlayerState() }
    private lateinit var playerHolder: PlayerHolder

    private val dataModel : DataModel by inject()

    private val disposables: CompositeDisposable = CompositeDisposable()

    private val adList : NotNullMutableLiveData<List<AdFile>> =
        NotNullMutableLiveData( emptyList<AdFile>())

    private val httpResult : NotNullMutableLiveData<ResultFlag> =
        NotNullMutableLiveData( ResultFlag(false))

    private var count = 0


    private var downloadManager: DownloadManager? = null



    // Android lifecycle hooks.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

//        val manager = this@VideoActivity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        val localLayoutParams = WindowManager.LayoutParams()
//        localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
//        localLayoutParams.gravity = Gravity.TOP
//        localLayoutParams.flags =
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//
//        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
//        localLayoutParams.height = (50 * getResources()
//            .getDisplayMetrics().scaledDensity).toInt()
//        localLayoutParams.format = PixelFormat.TRANSPARENT
//        val view = CustomViewGroup(this@VideoActivity)
//
//        manager.addView(view, localLayoutParams)

        Timber.i ( "VideoActivity onCreate Start")


        setContentView(R.layout.activity_video)


        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()

        Timber.i ( "Exoplayer starting")

        volumeControlStream = AudioManager.STREAM_MUSIC
        createMediaSession()
        createPlayer()

        startPlayer()
        activateMediaSession()

        Timber.i( "Exoplayer started")

        doUpsertID()

        httpResult.observe(this, Observer { flag ->
            if (flag.result) {
                doCheckAd()
            }
        })

        val fixedRateTimer = fixedRateTimer(
            "checkAd",
            false,
            30*60*1000, 30*60*1000){
            Timber.i ("FixedRateTimer time's up")
            doCheckAd()
        }



        adList.observe(this, Observer { list ->
            Timber.i( "Ad list is fetched " )
            Timber.i(  list.toString() )
            if (list.size == 0) {
                return@Observer
            }

            MediaCatalog.list.clear()


            count = list.size

            var stoppedFlag : Boolean = false
            list.forEach {

                Timber.i(  it.toString() )
                var filePath = File(Environment.getExternalStorageDirectory(), "Ads/")!!.absolutePath + "/${it.url}"
                MediaCatalog.list.add(
                    with(MediaDescriptionCompat.Builder()) {
                        setMediaUri(Uri.fromFile(File(filePath)))
                        build()
                    }
                )

                var file = File(filePath)
                if (file.exists()){
                    count -= 1
                } else {
                    if (!stoppedFlag){
                        Timber.i(  "Stop Exoplayer")
                        stopPlayer()
                        deactivateMediaSession()

                        releasePlayer()
                        releaseMediaSession()
                        stoppedFlag = true
                    }

                    Timber.i( "Ad is downloading")
                    prDownfile("https://vendor.hanbohui.org/v1/ads/${it.url}", it.url)
                }


            }

            if (count == 0) {

                Timber.i(  "Start Exoplayer")
                startPlayer()
                activateMediaSession()

            }
        })




        // While the user is in the app, the volume controls should adjust the music volume.

    }

    override fun onStart() {
        super.onStart()
        startPlayer()
        activateMediaSession()
    }

    override fun onStop() {
        super.onStop()
        stopPlayer()
        deactivateMediaSession()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        releasePlayer()
        releaseMediaSession()

    }

    override fun onResume() {
        super.onResume()
        Timber.i("Resumed")
    }

    // MediaSession related functions.
    private fun createMediaSession(): MediaSessionCompat = MediaSessionCompat(this, packageName)

    private fun createMediaSessionConnector(): MediaSessionConnector =
            MediaSessionConnector(mediaSession).apply {
                // If QueueNavigator isn't set, then mediaSessionConnector will not handle following
                // MediaSession actions (and they won't show up in the minimized PIP activity):
                // [ACTION_SKIP_PREVIOUS], [ACTION_SKIP_NEXT], [ACTION_SKIP_TO_QUEUE_ITEM]
                setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
                    override fun getMediaDescription(windowIndex: Int): MediaDescriptionCompat {
                        return MediaCatalog[windowIndex]
                    }
                })
            }


    // MediaSession related functions.
    private fun activateMediaSession() {
        // Note: do not pass a null to the 3rd param below, it will cause a NullPointerException.
        // To pass Kotlin arguments to Java varargs, use the Kotlin spread operator `*`.
        mediaSessionConnector.setPlayer(playerHolder.audioFocusPlayer, null)
        mediaSession.isActive = true
    }

    private fun deactivateMediaSession() {
        mediaSessionConnector.setPlayer(null, null)
        mediaSession.isActive = false
    }

    private fun releaseMediaSession() {
        mediaSession.release()
    }

    // ExoPlayer related functions.
    private fun createPlayer() {
        playerHolder = PlayerHolder(this, playerState, exoplayerview_activity_video)
    }

    private fun startPlayer() {
        playerHolder.start()

    }

    private fun stopPlayer() {
        playerHolder.stop()
    }

    private fun releasePlayer() {
        playerHolder.release()
    }

    // Picture in Picture related functions.
    override fun onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(
                    with(PictureInPictureParams.Builder()) {
                        val width = 16
                        val height = 9
                        setAspectRatio(Rational(width, height))
                        build()
                    })
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean,
                                               newConfig: Configuration?) {
        exoplayerview_activity_video.useController = !isInPictureInPictureMode
    }

    private fun getDeviceId() : String {

        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

    }

    fun addToDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    private fun doCheckAd() {
        Timber.i( "DoCheckAd started")
        addToDisposable(dataModel.httpApi.getAdFileList(getDeviceId()).with()
            .doOnSubscribe {  }
            .doOnSuccess {  }
            .doOnError {  }
            .subscribe({
                adList.value = it
                Timber.i( "DoCheckAd replied successfully")
            }, {
                // handle errors
            }))
    }

    private fun doUpsertID() {
        Timber.i(  "Upserting Id")
        val idJson = AdPlayerId(getDeviceId())
        addToDisposable(dataModel.httpApi.upsertId(idJson).with()
            .doOnSubscribe {  }
            .doOnSuccess {  }
            .doOnError {  }
            .subscribe({
                httpResult.value = it
                Timber.i(  "Upserting Id replied successfully" )
            }, {
                // handle errors
            }))
    }

    private fun download(title: String, description: String?, uri: String) {

        val request = DownloadManager.Request(Uri.parse(uri))

        val appName = getString(R.string.app_name)
        request.setTitle("$appName: $title")

        description?.let {
            request.setDescription(description)
        }

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

        request.setVisibleInDownloadsUi(false)

        val id = downloadManager?.enqueue(request) ?: -1L
        if (id == -1L) {
            return
        }

        DownloadManagerHelper.saveDownload(this, id)

        checkDownload(id)
    }

    private fun checkDownload(id: Long) {
        downloadManager?.let {

            val file = DownloadManagerHelper.getDownloadedFile(this, id)

            Timber.i( file.toString() + "\n\n")

            if (id in DownloadManagerHelper.getDownloads(this)) {
                Handler().postDelayed({ checkDownload(id) }, 500)
            }
        }
    }

    fun shouldAskPermissions() : Boolean {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
    }

    @TargetApi(23)
    fun askPermissions() {
        val permissions = arrayOf(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
        val requestCode = 200
        requestPermissions(permissions, requestCode)
    }

    fun prDownfile(urll:String,fileName:String){

            var mFile2: File? = File(Environment.getExternalStorageDirectory(), "Ads")
            System.out.println("File Foond " + mFile2!!.absolutePath)

        Timber.i(  urll )

            var downloadId = PRDownloader.download(urll, mFile2!!.absolutePath, fileName)
                .build()
                .setOnStartOrResumeListener(object : OnStartOrResumeListener {
                    override fun onStartOrResume() {
                        System.out.println(" start")
                    }
                })
                .setOnPauseListener(object : OnPauseListener {
                    override fun onPause() {
                    }
                })
                .setOnCancelListener(object : OnCancelListener {
                    override fun onCancel() {
                    }
                })
                .setOnProgressListener(object : OnProgressListener {
                    override fun onProgress(progress: Progress) {

                        var per = (progress.currentBytes.toFloat() / progress.totalBytes.toFloat()) * 100.00
                        //var perint = per*100
                        System.out.println(":: Per : " + per + "  : " + progress.currentBytes + "  : " + progress.totalBytes)

                    }
                })
                .start(object : OnDownloadListener {
                    override fun onError(error: com.downloader.Error?) {
                        System.out.println(" error " + error)
                    }

                    override fun onDownloadComplete() {
                        count -= 1

                        if (count == 0) {
                            startPlayer()
                            activateMediaSession()
                            count = -1
                        }
                    }


                })
            System.out.println(" called")

    }










}