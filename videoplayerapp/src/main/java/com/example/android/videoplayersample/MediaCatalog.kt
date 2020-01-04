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

import android.net.Uri
import android.os.Environment
import android.support.v4.media.MediaDescriptionCompat
import java.io.File

/**
 * Manages a set of media metadata that is used to create a playlist for [VideoActivity].
 */

open class MediaCatalog(public var list: MutableList<MediaDescriptionCompat>) :
        List<MediaDescriptionCompat> by list {

    companion object : MediaCatalog(mutableListOf())

    init {
        var filePath1 = File(Environment.getExternalStorageDirectory(), "Ads/")!!.absolutePath + "/ad1.mp4"
        var filePath2 = File(Environment.getExternalStorageDirectory(), "Ads/")!!.absolutePath + "/ad23.mp4"
        var filePath3 = File(Environment.getExternalStorageDirectory(), "Ads/")!!.absolutePath + "/game.mp4"

        // More creative commons, creative commons videos - https://www.blender.org/about/projects/
        list.add(
                with(MediaDescriptionCompat.Builder()) {
//                    setDescription("MP4 loaded over HTTP")
//                    setMediaId("1")
//                    // License - https://peach.blender.org/download/
////                    setMediaUri(Uri.parse("https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"))
                    setMediaUri(Uri.fromFile(File(filePath1)))
//                    setTitle("Short film Big Buck Bunny")
//                    setSubtitle("Streaming video")
                    build()
                })
        list.add(
                with(MediaDescriptionCompat.Builder()) {
//                    setDescription("MP4 loaded over HTTP")
//                    setMediaId("2")
//                    // License - https://archive.org/details/ElephantsDream
////                    setMediaUri(Uri.parse("https://ia800209.us.archive.org/20/items/ElephantsDream/ed_hd.mp4"))
                    setMediaUri(Uri.fromFile(File(filePath2)))
//                    setTitle("Short film Elephants Dream")
//                    setSubtitle("Streaming video")
                    build()
                })
        list.add(
                with(MediaDescriptionCompat.Builder()) {
//                    setDescription("MOV loaded over HTTP")
//                    setMediaId("3")
//                    // License - https://mango.blender.org/sharing/
////                    setMediaUri(Uri.parse("https://ia800209.us.archive.org/20/items/ElephantsDream/ed_hd.mp4"))
                    setMediaUri(Uri.fromFile(File(filePath3)))
//                    setTitle("Short film Tears of Steel")
//                    setSubtitle("Streaming audio")
                    build()
                })
    }
}