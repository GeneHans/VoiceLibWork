package com.example.voicelibwork.util

import android.content.Context
import android.util.Log
import android.widget.Toast

class Logger {

    private val TAG = "message"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun toast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    companion object {
        val instance: Logger by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { Logger() }
    }
}
