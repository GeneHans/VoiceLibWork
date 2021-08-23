package com.example.voicelibwork.systemtts

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.voicelibwork.listener.TTS
import com.example.voicelibwork.VoiceApplication
import com.example.voicelibwork.util.Logger
import java.util.*

class SystemTTS(var mContext: Context) : TTS, UtteranceProgressListener() {
    private lateinit var textToSpeech: TextToSpeech
    private var isSuccess: Boolean = true

    init {
        //初始化
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

    /**
     * 播报语音
     * @param playText:播报内容
     */
    override fun playText(playText: String) {
        if (!isSuccess) {
            Logger.instance.toast("系统不支持中文", mContext)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!isSpeaking()) {
                //此处必须要指定utteranceId,否则UtteranceProgressListener回调触发异常
                textToSpeech.speak(playText, TextToSpeech.QUEUE_ADD, null, "SystemTTS")
            } else {
                Logger.instance.toast("正在播报语音", mContext)
            }
        } else {
            Logger.instance.toast("系统低于6.0不支持系统tts", mContext)
        }
    }

    /**
     * 停止播报语音
     */
    override fun stopSpeak() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    /**
     * 表示当前是否正在播报语音
     */
    fun isSpeaking(): Boolean {
        return textToSpeech.isSpeaking
    }

    /**
     * 播报完成回调
     */
    override fun onDone(utteranceId: String?) {
        Logger.instance.d("语音播报完成   $utteranceId")
    }

    /**
     * 播报错误回调
     */
    override fun onError(utteranceId: String?) {
        Logger.instance.d("语音播报错误  $utteranceId")
    }

    /**
     * 播报开始回调
     */
    override fun onStart(utteranceId: String?) {
        Logger.instance.d("语音播报开始   $utteranceId")
    }

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SystemTTS(
                VoiceApplication.mContext
            )
        }
    }

}