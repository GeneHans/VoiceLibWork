package com.example.voicelibwork

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

class SystemTTS(var mContext: Context) : TTS, UtteranceProgressListener() {
    private lateinit var textToSpeech: TextToSpeech
    private var isSuccess: Boolean = true

    init {
        textToSpeech = TextToSpeech(mContext, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                //设置语言为中文
                var result = textToSpeech.setLanguage(Locale.CHINA)
                //设置音调，值越大越偏向于女声，值越小越偏向于男声，1.0为常规
                textToSpeech.setPitch(1.0f)
                //设置语速，默认为1.0
                textToSpeech.setSpeechRate(1.0f)
                textToSpeech.setOnUtteranceProgressListener(this@SystemTTS)
                if (result == TextToSpeech.LANG_NOT_SUPPORTED ||
                    result == TextToSpeech.LANG_MISSING_DATA
                ) {
                    //系统不支持中文播报
                    isSuccess = false
                }
            }
        })
    }

    override fun playText(playText: String) {
        if (!isSuccess) {
            Logger.instance.toast("系统不支持中文", mContext)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!isSpeaking()) {
                textToSpeech.speak(playText, TextToSpeech.QUEUE_ADD, null, null)
            } else {
                Logger.instance.toast("正在播报语音", mContext)
            }
        } else {
            Logger.instance.toast("系统低于6.0不支持系统tts", mContext)
        }
    }

    override fun stopSpeak() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    fun isSpeaking(): Boolean {
        return textToSpeech.isSpeaking
    }

    override fun onDone(utteranceId: String?) {
        Logger.instance.d("语音播报完成   $utteranceId")
    }

    override fun onError(utteranceId: String?) {
        Logger.instance.d("语音播报错误  $utteranceId")
    }

    override fun onStart(utteranceId: String?) {
        Logger.instance.d("语音播报开始   $utteranceId")
    }

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { SystemTTS(VoiceApplication.mContext) }
    }

}