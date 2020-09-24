package com.example.httpsender.parser

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import okhttp3.Response
import rxhttp.wrapper.callback.UriFactory
import rxhttp.wrapper.callback.query
import rxhttp.wrapper.entity.AppendUri
import java.io.File

/**
 * User: ljx
 * Date: 2020/9/11
 * Time: 17:43
 *
 * @param context Context
 * @param filename 文件名
 * @param relativePath  文件相对路径，可取值:
 * [Environment.DIRECTORY_DOWNLOADS]
 * [Environment.DIRECTORY_DCIM]
 * [Environment.DIRECTORY_PICTURES]
 * [Environment.DIRECTORY_MUSIC]
 * [Environment.DIRECTORY_MOVIES]
 * [Environment.DIRECTORY_DOCUMENTS]
 * ...
 */
class Android10DownloadFactory @JvmOverloads constructor(
    context: Context,
    private val filename: String,
    private val relativePath: String = Environment.DIRECTORY_DOWNLOADS
) : UriFactory(context) {

    /**
     * [MediaStore.Files.getContentUri]
     * [MediaStore.Downloads.EXTERNAL_CONTENT_URI]
     * [MediaStore.Audio.Media.EXTERNAL_CONTENT_URI]
     * [MediaStore.Video.Media.EXTERNAL_CONTENT_URI]
     * [MediaStore.Images.Media.EXTERNAL_CONTENT_URI]
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getInsertUri() = MediaStore.Downloads.EXTERNAL_CONTENT_URI

    override fun getAppendUri(): AppendUri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getInsertUri().query(context, filename, relativePath)
        } else {
            val file = File("${Environment.getExternalStorageDirectory()}/$relativePath/$filename")
            AppendUri(Uri.fromFile(file), file.length())
        }
    }

    override fun getUri(response: Response): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues().run {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath) //下载到指定目录
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)   //文件名
                //取contentType响应头作为文件类型
                put(MediaStore.MediaColumns.MIME_TYPE, response.body?.contentType().toString())
                context.contentResolver.insert(getInsertUri(), this)
            } ?: throw NullPointerException("Uri insert fail, Please change the file name")
        } else {
            val file = File("${Environment.getExternalStorageDirectory()}/$relativePath/$filename")
            Uri.fromFile(file)
        }
    }
}