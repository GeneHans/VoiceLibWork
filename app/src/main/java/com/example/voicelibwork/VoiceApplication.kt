package com.example.voicelibwork

import android.app.Application
import android.content.Context
import com.example.voicelibwork.systemtts.SystemTTS

class VoiceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        mContext = this
        SystemTTS.instance
    }

    companion object {
        lateinit var mContext: Context
    }
}