package com.example.voicelibwork

interface TTS {
    /**
     * 播报语音
     * @param playText:需要播报的语音
     */
    fun playText(playText:String)

    /**
     * 停止播报
     */
    fun stopSpeak()
}