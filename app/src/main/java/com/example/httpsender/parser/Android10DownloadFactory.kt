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
import rxhttp.wrapper.utils.query
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

    override fun query(): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getInsertUri().query(context, filename, relativePath)
        } else {
            val file = File("${Environment.getExternalStorageDirectory()}/$relativePath/$filename")
            Uri.fromFile(file)
        }
    }

    override fun insert(response: Response): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = getInsertUri().query(context, filename, relativePath)
            /*
             * 通过查找，要插入的Uri已经存在，就无需再次插入
             * 否则会出现新插入的文件，文件名被系统更改的现象，因为insert不会执行覆盖操作
             */
            if (uri != null) return uri
            ContentValues().run {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath) //下载到指定目录
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)   //文件名
                //取contentType响应头作为文件类型
                put(MediaStore.MediaColumns.MIME_TYPE, response.body?.contentType().toString())
                context.contentResolver.insert(getInsertUri(), this)
                //当相同路径下的文件，在文件管理器中被手动删除时，就会插入失败
            } ?: throw NullPointerException("Uri insert failed. Try changing filename")
        } else {
            val file = File("${Environment.getExternalStorageDirectory()}/$relativePath/$filename")
            Uri.fromFile(file)
        }
    }
}