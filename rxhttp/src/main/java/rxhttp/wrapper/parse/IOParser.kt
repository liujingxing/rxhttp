package rxhttp.wrapper.parse

import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.utils.IOUtil
import java.io.InputStream
import java.io.OutputStream

/**
 * User: ljx
 * Date: 2020/9/4
 * Time: 14:34
 */
abstract class IOParser<T> : Parser<T> {

    var callback: ProgressCallback? = null

    private var lastProgress = 0

    protected fun write(
        inStream: InputStream?,
        outStream: OutputStream?,
        contentLength: Long,
        offsetSize: Long = 0
    ) {
        val newContentLength = contentLength + offsetSize
        IOUtil.write(inStream, outStream) {
            callback?.apply {
                val currentSize = it + offsetSize
                //当前进度 = 当前已读取的字节 / 总字节
                val currentProgress = ((currentSize * 100f / newContentLength)).toInt()
                if (currentProgress > lastProgress) {
                    lastProgress = currentProgress
                    onProgress(currentProgress, currentSize, newContentLength)
                }
            }
        }
    }
}