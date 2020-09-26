package com.example.httpsender.kt

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

/**
 * User: ljx
 * Date: 2020/9/24
 * Time: 15:33
 */

fun Uri.dimQuery(context: Context, displayName: String) {
    context.contentResolver.query(this, null,
        "_display_name LIKE '%$displayName%'",null, null)?.use {
        while (it.moveToNext()) {
            val id = it.getString(it.getColumnIndex(MediaStore.MediaColumns._ID))
            val name = it.getString(it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
            val data = it.getString(it.getColumnIndex(MediaStore.MediaColumns.DATA))
            //注意: 通过这种方式获取的文件size，在文件被手动删除后，读取到的是不准确的
            val size = it.getString(it.getColumnIndex(MediaStore.MediaColumns.SIZE))
            val dateAdded = it.getString(it.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED))
            val dateModified = it.getString(it.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED))
            Log.e("LJX", "id=$id  size=$size  name=$name  data=$data  dateAdded=$dateAdded  dateModified=$dateModified")
        }
    }
}

fun Uri.dimDelete(context: Context, displayName: String) {
    val delete = context.contentResolver.delete(this, "_display_name LIKE '%$displayName%'", null)
    Log.e("LJX", "delete=$delete")
}

fun Uri.delete(context: Context, displayName: String) {
    val delete = context.contentResolver.delete(this, "_display_name=?", arrayOf(displayName))
    Log.e("LJX", "delete=$delete")
}