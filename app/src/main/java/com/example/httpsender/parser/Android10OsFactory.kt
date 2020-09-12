package com.example.httpsender.parser

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
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
class Android10OsFactory(val context: Context) : OutputStreamFactory<Uri>() {

    override fun getOutputStream(response: Response): OutputStreamWrapper<Uri> {

        val mimeType = response.body?.contentType().toString()
        val fileName = System.currentTimeMillis().toString()
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

        val uri = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.$extension") //文件名
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType) //文件类型
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) //共享文件夹，固定写法
            }
            context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        } else
            Uri.fromFile(File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?.absolutePath + File.separator + "$fileName.$extension")))
            ?: throw IllegalArgumentException()

        val os: OutputStream = context.contentResolver.openOutputStream(uri)
            ?: throw IllegalArgumentException()
        return os.toWrapper(uri)
    }
}