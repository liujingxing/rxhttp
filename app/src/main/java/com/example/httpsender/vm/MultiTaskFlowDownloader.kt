package com.example.httpsender.vm

import androidx.lifecycle.MutableLiveData
import com.example.httpsender.Tip
import com.example.httpsender.entity.DownloadTask
import com.example.httpsender.utils.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import okhttp3.internal.http2.StreamResetException
import okio.ByteString.Companion.encodeUtf8
import rxhttp.RxHttpPlugins
import rxhttp.toFlow
import rxhttp.wrapper.param.RxHttp
import java.io.File
import java.util.*

/**
 * User: ljx
 * Date: 2020/7/12
 * Time: 18:00
 */
object MultiTaskFlowDownloader {

    const val IDLE = 0             //未开始，闲置状态
    const val WAITING = 1          //等待中状态
    const val DOWNLOADING = 2      //下载中
    const val PAUSED = 3           //已暂停
    const val COMPLETED = 4        //已完成
    const val FAIL = 5             //下载失败
    const val CANCEL = 6           //取消状态，等待时被取消

    private const val MAX_TASK_COUNT = 3   //最大并发数

    @JvmStatic
    val liveTask = MutableLiveData<DownloadTask>() //用于刷新UI

    @JvmStatic
    val allTask = ArrayList<DownloadTask>() //所有下载任务
    private val waitTask = LinkedList<DownloadTask>() //等待下载的任务
    private val downloadingTask = LinkedList<DownloadTask>() //下载中的任务

    //记录每个文件的总大小，key为文件url
    private val lengthMap = HashMap<String, Long>()

    @JvmStatic
    fun addTasks(tasks: ArrayList<DownloadTask>) {
        val allTaskList = allTask
        tasks.forEach {
            if (!allTaskList.contains(it)) {
                val md5Key = it.url.encodeUtf8().md5().hex()
                val length = Preferences.getValue(md5Key, -1L)
                if (length != -1L) {
                    it.totalSize = length
                    it.currentSize = File(it.localPath).length()
                    it.progress = (it.currentSize * 100 / it.totalSize).toInt()
                    lengthMap[it.url] = length

                    if (it.currentSize > 0) {
                        it.state = PAUSED
                    }
                    if (it.totalSize == it.currentSize) {
                        //如果当前size等于总size，则任务文件下载完成，注意: 这个判断不是100%准确，最好能对文件做md5校验
                        it.state = COMPLETED
                    }
                }
                allTaskList.add(it)
            }
        }
    }

    //开始下载所有任务
    @JvmStatic
    fun startAllDownloadTask() {
        val allTaskList = allTask
        allTaskList.forEach {
            if (it.state != COMPLETED && it.state != DOWNLOADING) {
                download(it)
            }
        }
    }

    @JvmStatic
    fun download(task: DownloadTask) {
        if (downloadingTask.size >= MAX_TASK_COUNT) {
            //超过最大下载数，添加进等待队列
            task.state = WAITING
            updateTask(task)
            waitTask.offer(task)
            return
        }

        task.state = DOWNLOADING
        updateTask(task)
        downloadingTask.add(task)

        //如果想使用RxJava或Flow下载，更改以下代码即可
        CoroutineScope(Dispatchers.Main).launch {
            RxHttp.get(task.url)
                .tag(task.url) //记录tag，手动取消下载时用到
                .toFlow(task.localPath, true) {
                    //下载进度回调,0-100，仅在进度有更新时才会回调
                    task.progress = it.progress        //当前进度 0-100
                    task.currentSize = it.currentSize  //当前已下载的字节大小
                    task.totalSize = it.totalSize      //要下载的总字节大小
                    updateTask(task)
                    val key = task.url
                    val length = lengthMap[key]
                    if (length != task.totalSize) {
                        //服务器返回的文件总大小与本地的不一致，则更新
                        lengthMap[key] = task.totalSize
                        saveTotalSize(lengthMap)
                    }
                }.catch {
                    //手动取消下载时，会收到StreamResetException异常，不做任何处理
                    if (it !is StreamResetException) {
                        Tip.show("下载失败")
                        task.state = FAIL
                    }
                }.collect {
                    Tip.show("下载成功")
                    task.state = COMPLETED
                }

            //下载结束，不管任务成功还是失败，如果还有在等待的任务，都开启下一个任务
            updateTask(task)
            downloadingTask.remove(task)
            waitTask.poll()?.let { download(it) }
        }
    }


    private fun saveTotalSize(map: HashMap<String, Long>) {
        val editor = Preferences.getEditor()
        for ((key, value) in map) {
            val md5Key = key.encodeUtf8().md5().hex()
            editor.putLong(md5Key, value)
        }
        editor.commit()
    }


    //关闭所有任务
    @JvmStatic
    fun cancelAllTask() {
        var iterator = waitTask.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            task.state = CANCEL
            iterator.remove()
            updateTask(task)
        }

        iterator = downloadingTask.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            iterator.remove()
            RxHttpPlugins.cancelAll(task.url)
            task.state = CANCEL
            updateTask(task)
        }
    }

    //等待中->取消下载
    @JvmStatic
    fun removeWaitTask(task: DownloadTask) {
        waitTask.remove(task)
        task.state = CANCEL
        updateTask(task)
    }

    //暂停下载
    @JvmStatic
    fun pauseTask(task: DownloadTask) {
        //根据tag取消下载
        RxHttpPlugins.cancelAll(task.url)
        task.state = PAUSED
        updateTask(task)
    }

    @JvmStatic
    fun haveTaskExecuting(): Boolean {
        return waitTask.size > 0 || downloadingTask.size > 0
    }

    //发送通知，更新UI
    private fun updateTask(task: DownloadTask) {
        liveTask.value = task
    }
}