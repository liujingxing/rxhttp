package rxhttp.wrapper.parse

import kotlinx.coroutines.withContext
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.IOUtil
import rxhttp.wrapper.utils.LogUtil
import java.io.*
import kotlin.coroutines.CoroutineContext

/**
 * User: ljx
 * Date: 2020/9/5
 * Time: 14:09
 */
@Suppress("BlockingMethodInNonBlockingContext")
class SuspendDownloadParser(
    private val localPath: String,
    private val context: CoroutineContext? = null,
    private val progress: suspend (Progress) -> Unit,
) {

    private var lastProgress = 0

    @Throws(IOException::class)
    suspend fun onParse(response: Response): String {
        val body = ExceptionHelper.throwIfFatal(response)
        LogUtil.log(response, localPath)

        val contentLength = OkHttpCompat.getContentLength(response)
        val offsetSize = OkHttpCompat.getDownloadOffSize(response)?.offSize ?: 0
        val append = OkHttpCompat.header(response, "Content-Range") != null

        //创建文件
        val dstFile = File(localPath).apply {
            val parentFile = parentFile
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw IOException("Directory $parentFile create fail")
            }
        }

        //将输入流写出到文件
        write(body.byteStream(), FileOutputStream(dstFile, append), contentLength, offsetSize)
        return localPath
    }

    private suspend fun write(
        inStream: InputStream?,
        outStream: OutputStream?,
        contentLength: Long,
        offsetSize: Long = 0
    ) {
        val newContentLength = contentLength + offsetSize
        write(inStream, outStream) {
            val currentSize = it + offsetSize
            //当前进度 = 当前已读取的字节 / 总字节
            val currentProgress = ((currentSize * 100f / newContentLength)).toInt()
            if (currentProgress > lastProgress) {
                lastProgress = currentProgress
                val p = Progress(currentProgress, currentSize, newContentLength)
                if (context != null) {
                    withContext(context) {
                        progress.invoke(p)
                    }
                } else {
                    progress.invoke(p)
                }
            }
        }
    }


    @Throws(IOException::class)
    private suspend fun write(
        inStream: InputStream?,
        outStream: OutputStream?,
        progress: (suspend (Long) -> Unit)? = null
    ): Boolean {
        if (inStream == null || outStream == null) {
            throw IllegalArgumentException("inStream or outStream can not be null")
        }
        return try {
            val bytes = ByteArray(8 * 1024)
            var totalReadLength: Long = 0
            var readLength: Int
            while (inStream.read(bytes, 0, bytes.size).also { readLength = it } != -1) {
                outStream.write(bytes, 0, readLength)
                progress?.apply {
                    totalReadLength += readLength;
                    invoke(totalReadLength)
                }
            }
            true
        } finally {
            IOUtil.close(inStream, outStream)
        }
    }
}