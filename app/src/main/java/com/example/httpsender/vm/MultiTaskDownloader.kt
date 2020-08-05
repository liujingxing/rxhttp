package com.example.httpsender.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.httpsender.Tip
import com.example.httpsender.entity.DownloadTask
import com.rxjava.rxlife.RxLife
import com.rxjava.rxlife.ScopeViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import rxhttp.wrapper.param.RxHttp
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * User: ljx
 * Date: 2020/7/12
 * Time: 18:00
 */
class MultiTaskDownloader(application: Application) : ScopeViewModel(application) {

    companion object {
        const val MAX_TASK_COUNT = 3   //最大并发数

        const val IDLE = 0             //未开始，闲置状态
        const val WAITING = 1          //等待中状态
        const val DOWNLOADING = 2      //下载中
        const val PAUSED = 3           //已暂停
        const val COMPLETED = 4        //已完成
        const val FAIL = 5             //下载失败
        const val CANCEL = 6           //取消状态，等待时被取消
    }

    val allLiveTask = MutableLiveData<ArrayList<DownloadTask>>() //所有下载任务

    private val waitTask = LinkedList<DownloadTask>() //等待下载的任务
    private val downloadingTask = LinkedList<DownloadTask>() //下载中的任务

    fun addTasks(tasks: ArrayList<DownloadTask>) {
        val allTaskList = getAllTask()
        allTaskList.addAll(tasks)
        allLiveTask.value = allTaskList
    }

    //开始下载所有任务
    fun startAllDownloadTask() {
        val allTaskList = getAllTask()
        allTaskList.forEach { download(it) }
    }

    fun download(data: DownloadTask) {
        if (downloadingTask.size >= MAX_TASK_COUNT) {
            data.state = WAITING
            waitTask.offer(data)
            return
        }
        val destPath: String = getApplication<Application>().externalCacheDir.toString() + "/" + data.taskId + ".apk"
        val length = File(destPath).length()
        val disposable = RxHttp.get(data.url)
            .setRangeHeader(length, -1, true) //设置开始下载位置，结束位置默认为文件末尾
            .asDownload(destPath, AndroidSchedulers.mainThread()) {   //如果需要衔接上次的下载进度，则需要传入上次已下载的字节数length
                //下载进度回调,0-100，仅在进度有更新时才会回调
                data.progress = it.progress //当前进度 0-100
                data.currentSize = it.currentSize //当前已下载的字节大小
                data.totalSize = it.totalSize //要下载的总字节大小
                updateTask()
            }
            .doFinally {
                updateTask()
                //不管任务成功还是失败，如果还有在等待的任务，都开启下一个任务
                downloadingTask.remove(data)
                waitTask.poll()?.let { download(it) }
            }
            .to(RxLife.to(this)) //加入感知生命周期的观察者
            .subscribe({
                Tip.show("下载完成")
                data.state = COMPLETED
            }, {
                Tip.show("下载失败")
                data.state = FAIL
            })
        data.state = DOWNLOADING
        data.disposable = disposable
        downloadingTask.add(data)
    }

    //关闭所有任务
    fun cancelAllTask() {
        var iterator = waitTask.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            task.state = CANCEL
            iterator.remove()
        }

        iterator = downloadingTask.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            iterator.remove()
            val disposable = task.disposable
            RxHttp.dispose(disposable)
            task.state = CANCEL
        }
        updateTask()
    }

    //等待中->取消下载
    fun removeWaitTask(task: DownloadTask) {
        waitTask.remove(task)
        task.state = CANCEL
        updateTask()
    }

    //暂停下载
    fun pauseTask(task: DownloadTask) {
        val disposable = task.disposable
        if (!RxHttp.isDisposed(disposable)) {
            disposable.dispose()
            task.state = PAUSED
            updateTask()
        }
    }

    //发送通知，更新UI
    private fun updateTask() {
        val allTask = getAllTask()
        allLiveTask.value = allTask
    }

    private fun getAllTask(): ArrayList<DownloadTask> {
        return allLiveTask.value ?: ArrayList()
    }
}