package com.example.voicelibwork.bd

import android.content.ContentValues
import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.example.voicelibwork.util.FileUtil
import java.io.File
import java.io.IOException
import java.util.*

class OfflineResource(var context: Context) {
    //tts文件存放路径
    private val SAMPLE_DIR = "tts"

    //版本控制字段，每次升级百度TTS SDK都需要更新此字段,建议每次加1
    private val TTS_VERSION = "1"

    private var assets: AssetManager? = null
    private var destPath: String? = null
    private var rootPath: String? = null

    private var textFilename: String? = null
    private var modelFilename: String? = null

    //用来标识resource的首次文件初始化操作是否完成
    private var initFile = false

    private val mapInitied =
        HashMap<String, Boolean>()

    init {
        context = context.applicationContext
        assets = context.applicationContext.assets
        destPath = context.cacheDir
            .toString() + File.separator + SAMPLE_DIR + File.separator + TTS_VERSION
        rootPath = context.cacheDir.toString() + File.separator + SAMPLE_DIR
        textFilename = destPath + File.separator + "text.dat"
        modelFilename = destPath + File.separator + "model.dat"
    }

    /**
     * 清除旧版本文件夹
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun clearOldVersionFile() {
        val file = File(destPath)
        if (!file.exists()) {
            val rootFile = File(rootPath)
            FileUtil.instance.deleteDirs(rootFile)
            file.mkdirs()
        }
    }

    fun getModelFilename(): String? {
        return modelFilename
    }

    fun getTextFilename(): String? {
        return textFilename
    }

    fun getInitFile(): Boolean {
        return initFile
    }

    /**
     * 重新更新text文件及model文件
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun setOfflineVoiceResource() {
        val text = "text.dat"
        val modelStr = "model.dat"
        if (!isFileCanRead(textFilename)) {
            textFilename = copyAssetsFile(text)
        }
        if (!isFileCanRead(modelFilename)) {
            modelFilename = copyAssetsFile(modelStr)
        }
        initFile = true
    }

    /**
     * 判断文件是否存在和可读
     *
     * @param fileName
     * @return
     */
    fun isFileCanRead(fileName: String?): Boolean {
        return FileUtil.instance.fileIsExists(fileName) && FileUtil.instance.fileCanRead(fileName)
    }

    @Throws(IOException::class)
    private fun copyAssetsFile(sourceFilename: String): String? {
        val destFilename = "$destPath/$sourceFilename"
        var recover = false
        val existed = mapInitied[sourceFilename] // 启动时完全覆盖一次
        if (existed == null || !existed) {
            recover = true
        }
        FileUtil.instance.copyFromAssets(assets!!, sourceFilename, destFilename, recover)
        Log.i(ContentValues.TAG, "文件复制成功：$destFilename")
        return destFilename
    }

    /**
     * 删除destPath目录下的所有资源文件
     */
    fun removeInvalidFile() {
        val file = File(destPath)
        FileUtil.instance.deleteDirs(file)
    }

    fun needRemoveOldFile(): Boolean {
        val file = File(destPath)
        return !file.exists()
    }
}