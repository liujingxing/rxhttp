package rxhttp.wrapper.callback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rxhttp.wrapper.entity.Progress

/**
 * 下载进度回调
 * User: ljx
 * Date: 2020/3/22
 * Time: 00:48
 */
internal class ProgressCallbackImpl(
    private val offsetSize: Long,
    private val progress: (Progress) -> Unit
) : ProgressCallback {

    private var lastProgress = 0   //上次下载进度

    override fun onProgress(progress: Int, currentSize: Long, totalSize: Long) {
        //这里最多回调100次,仅在进度有更新时,才会回调
        val p = Progress(progress, currentSize, totalSize)
        if (offsetSize > 0) {
            p.addCurrentSize(offsetSize)
            p.addTotalSize(offsetSize)
            p.updateProgress()
            val currentProgress: Int = p.progress
            if (currentProgress <= lastProgress) return
            lastProgress = currentProgress
        }
        progress(p)
    }
}

/**
 * 下载进度回调，回调在suspend方法中回调
 * User: ljx
 * Date: 2020/3/22
 * Time: 00:48
 */
internal class SuspendProgressCallbackImpl(
    private val coroutine: CoroutineScope,  //协程，用于对进度回调切换线程
    private val offsetSize: Long,
    private val progress: suspend (Progress) -> Unit
) : ProgressCallback {

    private var lastProgress = 0   //上次下载进度

    override fun onProgress(progress: Int, currentSize: Long, totalSize: Long) {
        //这里最多回调100次,仅在进度有更新时,才会回调
        val p = Progress(progress, currentSize, totalSize)
        if (offsetSize > 0) {
            p.addCurrentSize(offsetSize)
            p.addTotalSize(offsetSize)
            p.updateProgress()
            val currentProgress: Int = p.progress
            if (currentProgress <= lastProgress) return
            lastProgress = currentProgress
        }
        coroutine.launch { progress(p) }
    }
}