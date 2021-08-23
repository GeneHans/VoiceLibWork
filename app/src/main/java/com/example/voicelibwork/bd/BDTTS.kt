package com.example.voicelibwork.bd

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.baidu.tts.client.*
import com.example.voicelibwork.listener.PlayListener
import com.example.voicelibwork.listener.TTS
import com.example.voicelibwork.VoiceApplication
import com.example.voicelibwork.util.Logger
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy

class BDTTS(var mContext: Context) : TTS {

    private var mSpeechSynthesizer: SpeechSynthesizer = SpeechSynthesizer.getInstance()
    private val mHandler = Handler(Looper.getMainLooper())
    var resource: OfflineResource? = null

    private var playListener: PlayListener? = null

    @Volatile
    private var offlineFailure = true

    @Volatile
    private var isSpeaking = false

    init {
        this.mSpeechSynthesizer.setContext(mContext)
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(object : SpeechSynthesizerListener {
            override fun onSynthesizeStart(s: String) {}
            override fun onSynthesizeDataArrived(s: String, bytes: ByteArray, i: Int, i2: Int) {}
            override fun onSynthesizeFinish(s: String) {}
            override fun onSpeechStart(s: String) {}
            override fun onSpeechProgressChanged(s: String, i: Int) {}
            override fun onSpeechFinish(s: String) {}
            override fun onError(s: String, speechError: SpeechError) {}
        })
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId("xxxxxxxxx")
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey(
                "xxxxxxxxxxxxxxxxxx",
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
        )
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0")
        // 设置声音大小
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "7")
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "6")
        initTTS(mContext)
    }

    /**
     * 检查TTS语音资源文件并初始化TTS
     *
     * @param context
     */
    private fun initTTS(context: Context) {
        if (resource == null) {
            resource = OfflineResource(context)
            //判断如果新的文本已经存在，并且合法。
            if (!resource!!.needRemoveOldFile() &&
                    checkOfflineResources(resource!!.getTextFilename(), resource!!.getModelFilename())) {
                if (initOffline()) {
                    mSpeechSynthesizer.release()
                }
            } else {
                initResourceFile()
            }
        } else {
            if (resource!!.getInitFile()) {
                val isOffLineReady: Boolean =
                        checkOfflineResources(resource!!.getTextFilename(), resource!!.getModelFilename())
                if (isOffLineReady) {
                    if (initOffline()) {
                        mSpeechSynthesizer.release()
                    }
                }
            } else {
                initResourceFile()
            }
        }
    }

    /**
     * 初始化BD TTS离线SDK
     * @return:返回初始化的结果
     */
    private fun initOffline(): Boolean {
//        Logger.get().d("开始执行初始化");
        //添加鉴权的SN参数，此方法要调用在auth方法之前
        mSpeechSynthesizer.setParam(
                SpeechSynthesizer.PARAM_AUTH_SN,
                "xxxxxxxxxxxxxxxxxxxx"
        )
        // 文本模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
        mSpeechSynthesizer.setParam(
                SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE,
                resource!!.getTextFilename()
        )
        // 声学模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
        mSpeechSynthesizer.setParam(
                SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                resource!!.getModelFilename()
        )
        //设置超时时间2s
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE_TIMEOUT, "2")
        //设置为WIFI 4G 3G 2G下在线优先( 如果在线连接百度服务器失败或者超时2s，那么切换成离线合成)， 其它网络状况（）离线合成
        //之前MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI 部分终端设备离线初始化失败
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        mSpeechSynthesizer.setParam(
                SpeechSynthesizer.PARAM_MIX_MODE,
                SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI
        )
        // 设置OffLine模式的合成策略
        val ttsErrorCode = mSpeechSynthesizer.initTts(TtsMode.OFFLINE)
        Logger.instance.d("百度TTS初始化code = $ttsErrorCode,mode = OFFLINE")
        //return (offlineFailure = ttsErrorCode != 0);
        offlineFailure = ttsErrorCode != 0
        return offlineFailure
    }

    /**
     * 启动新线程去加载resource中的首次数据文件
     */
    private fun initResourceFile() {
        Thread(Runnable {
            try {
                synchronized(resource!!) {
                    resource!!.clearOldVersionFile()
                    resource!!.setOfflineVoiceResource()
                    //                    Logger.get().d("启动加载数据线程：" + Thread.currentThread().getName());
                    val isOffLineReady: Boolean = checkOfflineResources(
                            resource!!.getTextFilename(),
                            resource!!.getModelFilename()
                    )
                    if (isOffLineReady) {
                        mHandler.post(reInitRunnable)
                    } else {
                        resource!!.removeInvalidFile()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }

    /**
     * 判断文件是否存在及合法
     *
     * @param text  text文件路径名称
     * @param model model文件的路径名称
     * @return:true合法;false不合法
     */
    private fun checkOfflineResources(
            text: String?,
            model: String?
    ): Boolean {
        //文件为null不合法
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(model))
            return false
        val filenames = arrayOf(text, model)
        for (path in filenames) {
            if(path.isNullOrEmpty())
                return false
            val f = File(path)
            if (!f.canRead()) {
                return false
            } else {
                //检测文件是否有效
                val verifyResult = SynthesizerTool.verifyModelFile(path)
                if (!verifyResult) {
                    Logger.instance.d("tts 文件校验失败：" + f.name)
                    return false
                }
            }
        }
        return true
    }

    //初始化操作的runnable,供主线程mHandler使用
    private val reInitRunnable = Runnable {
        Logger.instance.d("当前执行初始化的线程是：  " + Thread.currentThread().name);
        if (initOffline()) {
            mSpeechSynthesizer.release()
        }
    }

    override fun playText(playText: String) {

    }

    override fun stopSpeak() {
    }

    /**
     * 设置播报回调
     */
    fun setPlayListener(playListener: PlayListener?) {
        this.playListener = Proxy.newProxyInstance(PlayListener::class.java.classLoader, arrayOf(PlayListener::class.java)) { proxy, method, args ->
            mHandler.post {
                try {
                    method.invoke(playListener, args)
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }
            null
        } as PlayListener
    }

    @Synchronized
    fun speak(text: String) {
        if (offlineFailure) {
            return
        }
        try {
            if (TextUtils.isEmpty(text) || text.toByteArray(charset("GBK")).size > 1024) {
                Logger.instance.d("TTS fail, err = text too long, text = $text")
                    playListener?.onPlayError(-2)

                return
            }
            val result = mSpeechSynthesizer.speak(text, text)
            if (result < 0) {
                    playListener?.onPlayError(-1)

                return
            }
            isSpeaking = true
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            Logger.instance.d( "TTS failed, err = UnsupportedEncodingException")
                playListener?.onPlayError(-2)
        }
    }

    fun pause() {
        if (offlineFailure) {
            return
        }
        mSpeechSynthesizer.pause()
    }

    fun resume() {
        if (offlineFailure) {
            return
        }
        mSpeechSynthesizer.resume()
    }

    @Synchronized
    fun stop() {
        if (offlineFailure) {
            return
        }
        Logger.instance.d("tts stop")
        mSpeechSynthesizer.stop()
        isSpeaking = false
    }

    /**
     * release tts
     */
    fun release() {
        mSpeechSynthesizer.release()
        mHandler.removeCallbacks(reInitRunnable)
        playListener = null
    }

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { BDTTS(VoiceApplication.mContext) }
    }
}