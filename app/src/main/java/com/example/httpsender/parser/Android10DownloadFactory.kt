package com.example.httpsender.parser

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import okhttp3.Response
import rxhttp.wrapper.callback.UriFactory
import java.io.File

/**
 * Android 10文件下载可参照此类
 *
 * 断点下载时，必须要传入 queryUri、fileName 参数
 *
 * queryUri 参数可以理解为要查找的uri对应的文件夹
 * fileName  就是要查询的文件名
 *
 * 内部会在queryUri对应的文件夹下查找于fileName名字一样的文件，进而得到文件id及文件长度(也就是断点未知)
 * 如果没有查询到，就走正常的下载流程
 *
 * User: ljx
 * Date: 2020/9/11
 * Time: 17:43
 */
class Android10DownloadFactory @JvmOverloads constructor(
    context: Context,
    queryUri: Uri? = null,
    fileName: String? = null
) : UriFactory(context, queryUri, fileName) {

    override fun getUri(response: Response): Uri {
        val displayName = fileName ?: response.request.url.pathSegments.last()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues().run {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName) //文件名
                //取contentType响应头作为文件类型
                put(MediaStore.MediaColumns.MIME_TYPE, response.body?.contentType().toString())
                //下载到Download目录
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                val uri = queryUri ?: MediaStore.Downloads.EXTERNAL_CONTENT_URI
                context.contentResolver.insert(uri, this)
            } ?: throw NullPointerException("Uri insert fail, Please change the file name")
        } else {
            Uri.fromFile(File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), displayName))
        }
    }
}