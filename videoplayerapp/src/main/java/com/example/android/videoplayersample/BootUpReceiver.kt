package com.example.android.videoplayersample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootUpReceiver : BroadcastReceiver() {


    override fun onReceive(p0: Context?, p1: Intent?) {
        Thread.sleep(10000)

        val i = Intent(p0, VideoActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        p0?.startActivity(i)
    }
}