@file:JvmName("IOUtil")

package rxhttp.wrapper.utils

import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * User: ljx
 * Date: 2016/11/15
 * Time: 15:31
 */
private const val LENGTH_BYTE = 8 * 1024 //一次性读写的字节个数，用于字节读取

@Throws(IOException::class)
internal fun InputStream.writeTo(
    outStream: OutputStream,
    progress: ((Long) -> Unit)? = null
): Boolean {
    return try {
        val bytes = ByteArray(LENGTH_BYTE)
        var totalReadLength: Long = 0
        var readLength: Int
        while (read(bytes, 0, bytes.size).also { readLength = it } != -1) {
            outStream.write(bytes, 0, readLength)
            progress?.apply {
                totalReadLength += readLength
                invoke(totalReadLength)
            }
        }
        true
    } finally {
        close(this, outStream)
    }
}

internal fun close(vararg closeables: Closeable?) {
    for (closeable in closeables) {
        if (closeable == null) continue
        try {
            closeable.close()
        } catch (ignored: IOException) {
        }
    }
}