package com.example.android.videoplayersample.data.http

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AdFile(
//    @SerializedName("id") val id: Int,
    @SerializedName("title") val url: String
) : Parcelable

@Parcelize
class ResultFlag(
    @SerializedName("result") val result: Boolean
) : Parcelable

@Parcelize
class AdPlayerId(
    @SerializedName("id") val id: String
) : Parcelable