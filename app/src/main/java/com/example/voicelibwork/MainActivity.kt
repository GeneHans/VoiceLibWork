package com.example.voicelibwork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.voicelibwork.systemtts.SystemTTS
import com.example.voicelibwork.util.Logger

class MainActivity : AppCompatActivity() {
    private lateinit var tv1:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv1 = findViewById(R.id.tv1)
        tv1.setOnClickListener {
            Logger.instance.d("点击文本")
            SystemTTS.instance.playText("测试语音")
        }
    }
}