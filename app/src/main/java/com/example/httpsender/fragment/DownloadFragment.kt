package com.example.httpsender.fragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.httpsender.DownloadMultiActivity
import com.example.httpsender.R
import com.example.httpsender.databinding.DownloadFragmentBinding
import com.example.httpsender.entity.Url
import com.example.httpsender.kt.errorMsg
import com.example.httpsender.kt.show
import com.example.httpsender.kt.startActivity
import com.example.httpsender.parser.Android10DownloadFactory
import com.rxjava.rxlife.life
import com.rxjava.rxlife.lifeOnMain
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rxhttp.awaitResult
import rxhttp.toAppendDownload
import rxhttp.toDownload
import rxhttp.wrapper.param.RxSimpleHttp

/**
 * 使用Coroutine+OkHttp发请求
 * User: ljx
 * Date: 2020/4/24
 * Time: 18:16
 */
class DownloadFragment : BaseFragment<DownloadFragmentBinding>(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_fragment)
    }

    override fun DownloadFragmentBinding.onViewCreated(savedInstanceState: Bundle?) {
        click = this@DownloadFragment
    }

    //-------------------- Android 10下载 --------------------

    //Android 10协程下载，不带进度，兼容Android 10以下
    private fun DownloadFragmentBinding.coroutineDownload10(view: View) {
        lifecycleScope.launch {
            val factory = Android10DownloadFactory(requireContext(), "miaobo.apk")
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过@Domain注解生成的
            RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toDownload(factory)
                .awaitResult {
                    tvResult.append("\n下载完成, $it")
                }.onFailure {
                    tvResult.append("\n${it.errorMsg}")
                    //失败回调
                    it.show()
                }

        }
    }

    //Android 10协程下载，带进度，兼容Android 10以下
    private fun DownloadFragmentBinding.coroutineDownloadProgress10(view: View) {
        lifecycleScope.launch {
            val factory = Android10DownloadFactory(requireContext(), "miaobo.apk")
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过注解生成的
            RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toDownload(factory, Dispatchers.Main) {
                    //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    tvResult.append(it.toString())
                }.awaitResult {
                    tvResult.append("\n下载完成, $it")
                }.onFailure {
                    tvResult.append("\n${it.errorMsg}")
                    //失败回调
                    it.show()
                }
        }
    }

    /**
     * Android 10 协程断点下载，兼容Android 10以下
     *
     * 内部会在指定的表里查找指定路径及文件名的Uri，并读取到Uri对应资源的长度，将其设置为断点位置
     * 如果没有查询到，就走正常的下载流程
     */
    private fun DownloadFragmentBinding.coroutineAppendDownloadProgress10(view: View) {
        lifecycleScope.launch {
            val factory = Android10DownloadFactory(requireContext(), "miaobo.apk")
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过注解生成的
            RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toAppendDownload(factory, Dispatchers.Main) {
                    //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    tvResult.append(it.toString())
                }
                .awaitResult {
                    tvResult.append("\n下载完成, $it")
                }.onFailure {
                    //下载失败
                    tvResult.append("\n${it.errorMsg}")
                    it.show()
                }
        }
    }

    //Android 10 RxJava下载，不带进度，兼容Android 10以下
    private fun DownloadFragmentBinding.rxJavaDownload10(view: View) {
        val factory = Android10DownloadFactory(requireContext(), "miaobo.apk")
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asDownload(factory)
            .lifeOnMain(this@DownloadFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    //Android 10 RxJava下载，带进度，兼容Android 10以下
    private fun DownloadFragmentBinding.rxJavaDownloadProgress10(view: View) {
        val factory = Android10DownloadFactory(requireContext(), "miaobo.apk")
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asDownload(factory, AndroidSchedulers.mainThread()) {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }
            .life(this@DownloadFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    /**
     * Android 10 RxJava断点下载，兼容Android 10以下
     *
     * 内部会在指定的表里查找指定路径及文件名的Uri，并读取到Uri对应资源的长度，将其设置为断点位置
     * 如果没有查询到，就走正常的下载流程
     */
    private fun DownloadFragmentBinding.rxJavaAppendDownloadProgress10(view: View) {
        val factory = Android10DownloadFactory(requireContext(), "miaobo.apk")
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asAppendDownload(factory, AndroidSchedulers.mainThread()) {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }
            .life(this@DownloadFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    //-------------------- Android 10之前下载 --------------------

    //文件下载，不带进度
    private fun DownloadFragmentBinding.coroutineDownload(view: View) {
        lifecycleScope.launch {
            val destPath = "${requireContext().externalCacheDir}/${System.currentTimeMillis()}.apk"
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过@Domain注解生成的
            RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toDownload(destPath)
                .awaitResult {
                    tvResult.append("\n下载完成, $it")
                }.onFailure {
                    tvResult.append("\n${it.errorMsg}")
                    //失败回调
                    it.show()
                }
        }
    }

    //文件下载，带进度
    private fun DownloadFragmentBinding.coroutineDownloadProgress(view: View) {
        lifecycleScope.launch {
            val destPath = "${requireContext().externalCacheDir}/${System.currentTimeMillis()}.apk"
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过注解生成的
            RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toDownload(destPath, Dispatchers.Main) {
                    //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    tvResult.append(it.toString())
                }
                .awaitResult {
                    tvResult.append("\n下载完成, $it")
                }.onFailure {
                    tvResult.append("\n${it.errorMsg}")
                    //失败回调
                    it.show()
                }

        }
    }

    //断点下载，带进度
    private fun DownloadFragmentBinding.coroutineAppendDownloadProgress(view: View) {
        lifecycleScope.launch {
            val destPath = "${requireContext().externalCacheDir}/Miaobo.apk"
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过注解生成的
            RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toAppendDownload(destPath, Dispatchers.Main) {
                    //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    tvResult.append(it.toString())
                }
                .awaitResult {
                    tvResult.append("\n下载完成, $it")
                }.onFailure {
                    //下载失败
                    tvResult.append("\n${it.errorMsg}")
                    it.show()
                }
        }
    }

    //文件下载，不带进度
    private fun DownloadFragmentBinding.rxJavaDownload(view: View) {
        val destPath = "${requireContext().externalCacheDir}/${System.currentTimeMillis()}.apk"
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asDownload(destPath)
            .lifeOnMain(this@DownloadFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    //文件下载，带进度
    private fun DownloadFragmentBinding.rxJavaDownloadProgress(view: View) {
        val destPath = "${requireContext().externalCacheDir}/${System.currentTimeMillis()}.apk"
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asDownload(destPath, AndroidSchedulers.mainThread()) {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }
            .life(this@DownloadFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }


    //断点下载，带进度
    private fun DownloadFragmentBinding.rxJavaAppendDownloadProgress(view: View) {
        val destPath = "${requireContext().externalCacheDir}/Miaobo.apk"
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asAppendDownload(destPath, AndroidSchedulers.mainThread()) {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }
            .life(this@DownloadFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    private fun DownloadFragmentBinding.clearLog(view: View) {
        tvResult.text = ""
        tvResult.setBackgroundColor(Color.TRANSPARENT)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onClick(v: View) = mBinding.run {
        when (v.id) {
            R.id.coroutine_download10 -> coroutineDownload10(v)
            R.id.coroutine_downloadProgress10 -> coroutineDownloadProgress10(v)
            R.id.coroutine_appendDownloadProgress10 -> coroutineAppendDownloadProgress10(v)
            R.id.rxjava_download10 -> rxJavaDownload10(v)
            R.id.rxjava_downloadProgress10 -> rxJavaDownloadProgress10(v)
            R.id.rxjava_appendDownloadProgress10 -> rxJavaAppendDownloadProgress10(v)
            R.id.coroutine_download -> coroutineDownload(v)
            R.id.coroutine_downloadProgress -> coroutineDownloadProgress(v)
            R.id.coroutine_appendDownloadProgress -> coroutineAppendDownloadProgress(v)
            R.id.rxjava_download -> rxJavaDownload(v)
            R.id.rxjava_downloadProgress -> rxJavaDownloadProgress(v)
            R.id.rxjava_appendDownloadProgress -> rxJavaAppendDownloadProgress(v)
            R.id.multitaskDownload -> startActivity(DownloadMultiActivity::class)
            R.id.bt_clear -> clearLog(v)
        }
    }
}