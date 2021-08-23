package com.example.voicelibwork.util

import android.content.Context
import android.content.res.AssetManager
import android.os.Environment
import android.text.TextUtils
import com.example.voicelibwork.VoiceApplication
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by herr.wang on 2017/6/21.
 */
class FileUtil(var context: Context) {
    private val LIB_DIR = "voiceLib"

    /**
     * 获取到SD卡的路径
     * @return
     */
    fun getSDCardPath(): String? {
        return if (Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED, ignoreCase = true)
        ) {
            Environment.getExternalStorageDirectory().absolutePath
        } else
            null
    }

    fun getLibDir(): String {
        val appPath = StringBuilder()
        var rootPath = getSDCardPath() ?: context.cacheDir.absolutePath
        appPath.append(rootPath).append(File.separator)
            .append(LIB_DIR).append(File.separator)
        val dir = File(appPath.toString())
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return appPath.toString()
    }

    fun getExternalCacheDir(): String? {
        return if (context.externalCacheDir != null) {
            context.externalCacheDir!!.absolutePath.toString() + File.separator
        } else
            null
    }

    /**
     * 创建文件
     * @param fileName:文件名称
     * @return:true表示创建文件成功，false表示创建文件失败；如果写入文件名为null返回true
     */
    fun createFile(fileName: String?): Boolean {
        return createFile(null, fileName)
    }

    /**
     * 创建文件
     * @param folderName:文件夹名称，没有则默认为libDir
     * @param fileName:文件名称
     * @return:true表示创建文件成功，false表示创建文件失败；如果写入文件名为null返回true
     */
    fun createFile(folderName: String?, fileName: String?): Boolean {
        val dir: File = if (TextUtils.isEmpty(folderName)) {
            File(getLibDir())
        } else {
            File(getLibDir() + folderName)
        }
        //如果文件目录不存在则创建文件目录
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return if (TextUtils.isEmpty(fileName)) {
            true
        } else {
            val file = File(dir.absolutePath, fileName)
            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: IOException) {
                    return false
                }
            }
            true
        }
    }

    //判断文件是否存在
    /**
     * 判断文件是否存在
     * @param filePath:文件名称
     * @return :
     * 1. 文件存在返回true;
     * 2. 文件不存在返回false;
     * 3. 文件名为null返回false
     */
    fun fileIsExists(filePath: String?): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return false
        }
        try {
            val f = File(filePath)
            if (!f.exists()) {
                return false
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun fileCanRead(filename: String?): Boolean {
        if (TextUtils.isEmpty(filename))
            return false
        val f = File(filename)
        return f.canRead()
    }

    fun makeDir(dirPath: String?): Boolean {
        if (TextUtils.isEmpty(dirPath))
            return false
        val file = File(dirPath)
        return if (!file.exists()) {
            file.mkdirs()
        } else {
            true
        }
    }

    @Throws(IOException::class)
    fun copyFromAssets(
        assets: AssetManager,
        source: String?,
        dest: String,
        isCover: Boolean
    ) {
        val file = File(dest)
        if (isCover || !isCover && !file.exists()) {
            var `is`: InputStream? = null
            var fos: FileOutputStream? = null
            try {
                `is` = assets.open(source!!)
                val path = dest
                fos = FileOutputStream(path)
                val buffer = ByteArray(1024)
                var size = 0
                while (`is`.read(buffer, 0, 1024).also { size = it } >= 0) {
                    fos.write(buffer, 0, size)
                }
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } finally {
                        `is`?.close()
                    }
                }
            }
        }
    }

    /**
     * 删除文件夹下所有文件及文件夹
     *
     * @param dirFile
     * @return
     */
    fun deleteDirs(dirFile: File?): Boolean {
        return deleteDirs("", dirFile)
    }

    /**
     * 删除文件夹下所有文件及文件夹，保留根目录
     *
     * @param rootDir
     * @param dirFile
     * @return
     */
    @JvmOverloads
    fun deleteDirs(
        rootDir: String,
        dirFile: File?,
        exceptPath: String? = ""
    ): Boolean {
        return try {
            if (dirFile != null && dirFile.exists() && dirFile.isDirectory) {
                if (!TextUtils.isEmpty(exceptPath) && dirFile.path.contains(exceptPath!!)) {
                    return true
                }
                for (f: File in dirFile.listFiles()) {
                    if (f.isFile) {
                        f.delete()
                    } else if (f.isDirectory) {
                        deleteDirs(rootDir, f, exceptPath)
                    }
                }
                if (rootDir != dirFile.path) {
                    return dirFile.delete()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            FileUtil(
                VoiceApplication.mContext
            )
        }
    }
}