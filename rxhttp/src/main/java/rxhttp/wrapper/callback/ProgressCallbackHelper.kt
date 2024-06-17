package rxhttp.wrapper.callback

import android.os.SystemClock
import rxhttp.wrapper.utils.Speeder


/**
 * User: ljx
 * Date: 2024/6/17
 * Time: 11:31
 */
class ProgressCallbackHelper(
    //上传/下载进度回调最小周期, 值越小，回调事件越多，设置一个合理值，可避免密集回调
    private val minPeriod: Int,
    private var callback: ProgressCallback
) {
    private var currentLength = 0L
    private var lastTime = 0L

    private lateinit var speeder: Speeder

    fun onStart(offSize: Long) {
        currentLength = offSize
        lastTime = SystemClock.elapsedRealtime()
        speeder = Speeder(offSize, lastTime)
    }

    /**
     * @param onceByteLength 单次上传/下载的字节长度，有可能为-1，下载总长度未知时，下载完成会传入-1
     * @param contentLength 上传/下载的总长度, 有可能为-1，代表总长度未知，下载时，有可能拿不到总长度
     */
    fun onProgress(onceByteLength: Long, contentLength: Long) {
        if (onceByteLength != -1L) {
            currentLength += onceByteLength
        }
        val completed = currentLength == contentLength || onceByteLength == -1L  //上传/下载是否完成
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastTime >= minPeriod || completed) {
            val averageSpeed = speeder.updateSpeed(currentLength, currentTime, completed)
            val totalSize = if (completed) currentLength else contentLength
            callback.onProgress(currentLength, totalSize, averageSpeed)
            lastTime = currentTime
        }
    }
}
