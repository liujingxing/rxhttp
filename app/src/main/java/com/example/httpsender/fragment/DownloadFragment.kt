package com.example.httpsender.fragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.rxLifeScope
import com.example.httpsender.DownloadMultiActivity
import com.example.httpsender.R
import com.example.httpsender.databinding.DownloadFragmentBinding
import com.example.httpsender.entity.Url
import com.example.httpsender.kt.errorMsg
import com.example.httpsender.kt.show
import com.example.httpsender.kt.startActivity
import com.example.httpsender.parser.Android10DownloadFactory
import com.rxjava.rxlife.lifeOnMain
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
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

    //文件下载，不带进度
    private fun coroutineDownload10(view: View) {
        rxLifeScope.launch({
            val factory = Android10DownloadFactory(requireContext())
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过@Domain注解生成的
            val result = RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toDownload(factory)
                .await()
            mBinding.tvResult.append("\n下载完成, $result")
        }, {
            mBinding.tvResult.append("\n${it.errorMsg}")
            //失败回调
            it.show()
        })
    }

    //文件下载，带进度
    private fun coroutineDownloadProgress10(view: View) {
        rxLifeScope.launch({
            val factory = Android10DownloadFactory(requireContext())
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过注解生成的
            val result = RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toDownload(factory, Dispatchers.Main) {
                    //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    mBinding.tvResult.append(it.toString())
                }
                .await()
            mBinding.tvResult.append("\n下载完成, $result")
        }, {
            mBinding.tvResult.append("\n${it.errorMsg}")
            //失败回调
            it.show()
        })
    }

    /**
     * Android 10 断点下载，构建Android10DownloadFactory对象时，必须要传入 queryUri、fileName 参数
     *
     * queryUri 参数可以理解为要查找的uri对应的文件夹
     * fileName  就是要查询的文件名
     *
     * 内部会在queryUri对应的文件夹下查找于fileName名字一样的文件，进而得到文件id及文件长度(也就是断点未知)
     * 如果没有查询到，就走正常的下载流程
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun coroutineAppendDownloadProgress10(view: View) {
        rxLifeScope.launch({
            val queryUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val factory = Android10DownloadFactory(requireContext(), queryUri, "miaobo.apk")
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过注解生成的
            val result = RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toAppendDownload(factory, Dispatchers.Main) {
                    //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    mBinding.tvResult.append(it.toString())
                }
                .await()
            mBinding.tvResult.append("\n下载完成, $result")
        }, {
            //下载失败
            mBinding.tvResult.append("\n${it.errorMsg}")
            it.show()
        })
    }

    //文件下载，不带进度
    private fun rxJavaDownload10(view: View) {
        val factory = Android10DownloadFactory(requireContext())
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asDownload(factory)
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                mBinding.tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    //文件下载，带进度
    private fun rxJavaDownloadProgress10(view: View) {
        val factory = Android10DownloadFactory(requireContext())
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asDownload(factory, AndroidSchedulers.mainThread()) {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                mBinding.tvResult.append(it.toString())
            }
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                mBinding.tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    /**
     * Android 10 断点下载，构建Android10DownloadFactory对象时，必须要传入 queryUri、fileName 参数
     *
     * queryUri 参数可以理解为要查找的uri对应的文件夹
     * fileName  就是要查询的文件名
     *
     * 内部会在queryUri对应的文件夹下查找于fileName名字一样的文件，进而得到文件id及文件长度(也就是断点未知)
     * 如果没有查询到，就走正常的下载流程
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun rxJavaAppendDownloadProgress10(view: View) {
        val queryUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val factory = Android10DownloadFactory(requireContext(), queryUri, "miaobo.apk")
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asAppendDownload(factory, AndroidSchedulers.mainThread()) {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                mBinding.tvResult.append(it.toString())
            }
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                mBinding.tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    //-------------------- Android 10之前下载 --------------------

    //文件下载，不带进度
    private fun coroutineDownload(view: View) {
        rxLifeScope.launch({
            val destPath = "${requireContext().externalCacheDir.toString()}/${System.currentTimeMillis()}.apk"
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过@Domain注解生成的
            val result = RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toDownload(destPath)
                .await()
            mBinding.tvResult.append("\n下载完成, $result")
        }, {
            mBinding.tvResult.append("\n${it.errorMsg}")
            //失败回调
            it.show()
        })
    }

    //文件下载，带进度
    private fun coroutineDownloadProgress(view: View) {
        rxLifeScope.launch({
            val destPath = "${requireContext().externalCacheDir.toString()}/${System.currentTimeMillis()}.apk"
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过注解生成的
            val result = RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toDownload(destPath, Dispatchers.Main) {
                    //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    mBinding.tvResult.append(it.toString())
                }
                .await()
            mBinding.tvResult.append("\n下载完成, $result")
        }, {
            mBinding.tvResult.append("\n${it.errorMsg}")
            //失败回调
            it.show()
        })
    }

    //断点下载，带进度
    private fun coroutineAppendDownloadProgress(view: View) {
        rxLifeScope.launch({
            val destPath = "${requireContext().externalCacheDir.toString()}/Miaobo.apk"
            //下载使用非默认域名，故这里使用RxSimpleHttp类发送请求，RxSimpleHttp类是通过注解生成的
            val result = RxSimpleHttp.get(Url.DOWNLOAD_URL)
                .toAppendDownload(destPath, Dispatchers.Main) {
                    //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    mBinding.tvResult.append(it.toString())
                }
                .await()
            mBinding.tvResult.append("\n下载完成, $result")
        }, {
            //下载失败
            mBinding.tvResult.append("\n${it.errorMsg}")
            it.show()
        })
    }

    //文件下载，不带进度
    private fun rxJavaDownload(view: View) {
        val destPath = "${requireContext().externalCacheDir.toString()}/${System.currentTimeMillis()}.apk"
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asDownload(destPath)
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                mBinding.tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    //文件下载，带进度
    private fun rxJavaDownloadProgress(view: View) {
        val destPath = "${requireContext().externalCacheDir.toString()}/${System.currentTimeMillis()}.apk"
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asDownload(destPath, AndroidSchedulers.mainThread()) {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                mBinding.tvResult.append(it.toString())
            }
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                mBinding.tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }


    //断点下载，带进度
    private fun rxJavaAppendDownloadProgress(view: View) {
        val destPath = "${requireContext().externalCacheDir.toString()}/Miaobo.apk"
        RxSimpleHttp.get(Url.DOWNLOAD_URL)
            .asAppendDownload(destPath, AndroidSchedulers.mainThread()) {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                mBinding.tvResult.append(it.toString())
            }
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                mBinding.tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    private fun clearLog(view: View) {
        mBinding.tvResult.text = ""
        mBinding.tvResult.setBackgroundColor(Color.TRANSPARENT)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onClick(v: View) {
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