package rxhttp.wrapper.parse

import kotlinx.coroutines.withContext
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.IOUtil
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.CoroutineContext

/**
 * User: ljx
 * Date: 2020/9/5
 * Time: 14:09
 */
@Suppress("BlockingMethodInNonBlockingContext")
class SuspendStreamParser(
    private val outputStream: OutputStream,
    private val context: CoroutineContext? = null,
    private val progress: suspend (Progress) -> Unit,
) {

    private var lastProgress = 0

    @Throws(IOException::class)
    suspend fun onParse(response: Response): String {
        val body = ExceptionHelper.throwIfFatal(response)
        LogUtil.log(response, "")

        val contentLength = OkHttpCompat.getContentLength(response)
        val offsetSize = OkHttpCompat.getDownloadOffSize(response)?.offSize ?: 0

        //将输入流写出到文件
        write(body.byteStream(), outputStream, contentLength, offsetSize)
        return ""
    }

    private suspend fun write(
        inStream: InputStream?,
        outStream: OutputStream?,
        contentLength: Long,
        offsetSize: Long = 0
    ) {
        val newContentLength = contentLength + offsetSize
        IOUtil.suspendWrite(inStream, outStream) {
            val currentSize = it + offsetSize
            //当前进度 = 当前已读取的字节 / 总字节
            val currentProgress = ((currentSize * 100f / newContentLength)).toInt()
            if (currentProgress > lastProgress) {
                lastProgress = currentProgress
                val p = Progress(currentProgress, currentSize, newContentLength)
                if (context != null) {
                    withContext(context) { progress.invoke(p) }
                } else {
                    progress.invoke(p)
                }
            }
        }
    }
}