package com.example.httpsender.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.httpsender.DownloadMultiActivity
import com.example.httpsender.Tip
import com.example.httpsender.entity.DownloadTask
import com.rxjava.rxlife.RxLife
import com.rxjava.rxlife.ScopeViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import rxhttp.wrapper.param.RxHttp
import java.io.File

/**
 * User: ljx
 * Date: 2020/7/12
 * Time: 18:00
 */
class MultiTaskDownloader(application: Application) : ScopeViewModel(application) {

    val allLiveTask = MutableLiveData<ArrayList<DownloadTask>>() //所有下载任务

    private val waitTask = ArrayList<DownloadTask>() //等待下载的任务
    private val downloadingTask = ArrayList<DownloadTask>() //下载中的任务

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

    //关闭所有任务
    fun cancelAllTask() {
        var iterator = waitTask.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            next.state = 6
            iterator.remove()
        }

        iterator = downloadingTask.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            iterator.remove()
            val disposable = next.disposable
            if (!RxHttp.isDisposed(disposable)) {
                disposable.dispose()
            }
            next.state = 6
        }
        val allTask = getAllTask()
        allLiveTask.value = allTask
    }

    fun download(data: DownloadTask) {
        if (downloadingTask.size >= DownloadMultiActivity.MAX_TASK_COUNT) {
            data.state = 1
            waitTask.add(data)
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
                val allTask = getAllTask()
                allLiveTask.value = allTask
            }
            .doFinally {
                //不管任务成功还是失败，如果还有在等待的任务，都开启下一个任务
                downloadingTask.remove(data)
                if (waitTask.size > 0) download(waitTask.removeAt(0))
            }
            .to(RxLife.to(this)) //加入感知生命周期的观察者
            .subscribe({
                Tip.show("下载完成$it")
                data.state = 4
                val allTask = getAllTask()
                allLiveTask.value = allTask
            }, {
                data.state = 5
            })
        data.state = 2
        downloadingTask.add(data)
        data.disposable = disposable
    }

    fun removeWaitTask(task: DownloadTask) {
        //等待中->取消下载
        waitTask.remove(task)
        task.state = 6
        val allTask = getAllTask()
        allLiveTask.value = allTask
    }

    //暂停下载
    fun pauseTask(task: DownloadTask) {
        val disposable = task.disposable
        if (!RxHttp.isDisposed(disposable)) {
            disposable.dispose()
            task.state = 3
            val allTask = getAllTask()
            allLiveTask.value = allTask
        }
    }

    private fun getAllTask(): ArrayList<DownloadTask> {
        return allLiveTask.value ?: ArrayList()
    }
}