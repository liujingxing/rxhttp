package rxhttp.wrapper.utils

import java.util.concurrent.LinkedBlockingQueue

/**
 * User: ljx
 * Date: 2024/6/16
 * Time: 16:21
 */
class Speeder(
    private var lastLength: Long,
    private var lastTime: Long,
) {

    private var lastSpeed: Long? = null
    //缓存近5秒内每秒的速速，回调时，取平均值，避免抖动过大
    private val queue = LinkedBlockingQueue<Long>(5)

    fun updateSpeed(currentLength: Long, currentTime: Long, complete: Boolean = false): Long {
        var diff = currentTime - lastTime
        //速度最小更新周期为1s，第一次跟最后一次不受时长限制
        if (diff >= 1000 || complete || queue.isEmpty()) {
            if (diff == 0L) diff = 1L
            val speed = (currentLength - lastLength) * 1000 / diff
            while (!queue.offer(speed)) {
                queue.poll()
            }
            lastSpeed = null
            lastLength = currentLength
            lastTime = currentTime
        }
        return averageSpeed()
    }

    private fun averageSpeed() = lastSpeed ?: queue.average().toLong().also { lastSpeed = it }
}