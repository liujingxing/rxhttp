package com.example.httpsender.parser

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import okhttp3.Response
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.entity.OutputStreamWrapper
import rxhttp.wrapper.entity.toWrapper
import java.io.File
import java.io.OutputStream

/**
 * Android 10文件下载可参照此类
 * User: ljx
 * Date: 2020/9/11
 * Time: 17:43
 */
class Android10DownloadFactory @JvmOverloads constructor(
    private val context: Context,
    private var fileName: String? = null
) : OutputStreamFactory<Uri>() {

    override fun getOutputStream(response: Response): OutputStreamWrapper<Uri> {
        if (fileName == null) {
            //如果没有传入文件名，就取下载链接中的文件名
            fileName = response.request.url.pathSegments.last()
        }
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues().run {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName) //文件名
                //取contentType响应头作为文件类型
                put(MediaStore.MediaColumns.MIME_TYPE, response.body?.contentType().toString())
                //下载到Download目录
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, this)
            } ?: throw NullPointerException("Uri can not be null")
        } else {
            Uri.fromFile(File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName))
        }
        val os: OutputStream = context.contentResolver.openOutputStream(uri)
            ?: throw NullPointerException("OutputStream can not be null")
        return os.toWrapper(uri)
    }
}