package com.example.voicelibwork.listener

/**
 * 播报结果回调
 */
interface PlayListener {
    /**
     * 播报结束
     */
    fun onPlayFinish()

    /**
     * 播报错误
     * @param errorCode:错误码
     */
    fun onPlayError(errorCode: Int)
}