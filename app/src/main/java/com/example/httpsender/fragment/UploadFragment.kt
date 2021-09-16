package com.example.httpsender.fragment

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.httpsender.R
import com.example.httpsender.databinding.UploadFragmentBinding
import com.example.httpsender.entity.Url
import com.example.httpsender.kt.errorMsg
import com.example.httpsender.kt.show
import com.rxjava.rxlife.life
import com.rxjava.rxlife.lifeOnMain
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import rxhttp.awaitResult
import rxhttp.toFlow
import rxhttp.toStr
import rxhttp.wrapper.param.RxHttp
import java.io.File

/**
 * android 10 使用协程、RxJava上传案例
 *
 * Android 10 调用一系列 addPart 方法添加 Uri 对象上传文件
 *
 * Android 10 以下调用一系列 addFile 方法添加 File 对象上传文件
 *
 * User: ljx
 * Date: 2020/4/24
 * Time: 18:16
 */
class UploadFragment : BaseFragment<UploadFragmentBinding>(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_fragment)
    }

    override fun UploadFragmentBinding.onViewCreated(savedInstanceState: Bundle?) {
        click = this@UploadFragment
    }

    //-------------------- Android 10上传 --------------------

    //协程文件上传，不带进度
    private suspend fun UploadFragmentBinding.coroutineUpload10(v: View) {
        //真实环境，需要调用文件选择器，拿到Uri对象
        val uri = Uri.parse("content://media/external/downloads/13417")
        RxHttp.postForm(Url.UPLOAD_URL)
            .addPart(requireContext(), "uploaded_file", uri)
            .toStr()
            .awaitResult {
                tvResult.append("\n上传成功 : $it")
            }.onFailure {
                tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            }
    }

    //协程上传文件，带进度
    private suspend fun UploadFragmentBinding.coroutineUploadProgress10(v: View) {
        //真实环境，需要调用文件选择器，拿到Uri对象
        val uri = Uri.parse("content://media/external/downloads/13417")
        RxHttp.postForm(Url.UPLOAD_URL)
            .addPart(requireContext(), "uploaded_file", uri)
            .toFlow<String> {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize //要上传的总字节大小
                tvResult.append("\n" + it.toString())
            }.catch {
                tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            }.collect {
                tvResult.append("\n上传成功 : $it")
            }
    }


    //RxJava文件上传，不带进度
    private fun UploadFragmentBinding.rxJavaUpload10(v: View) {
        //真实环境，需要调用文件选择器，拿到Uri对象
        val uri = Uri.parse("content://media/external/downloads/13417")
        RxHttp.postForm(Url.UPLOAD_URL)
            .addPart(requireContext(), "uploaded_file", uri)
            .asString()
            .lifeOnMain(this@UploadFragment)   //页面销毁，自动关闭请求，并在主线程回调
            .subscribe({
                tvResult.append("\n上传成功 : $it")
            }, {
                tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            })
    }

    //RxJava上传文件，带进度
    private fun UploadFragmentBinding.rxJavaUploadProgress10(v: View) {
        //真实环境，需要调用文件选择器，拿到Uri对象
        val uri = Uri.parse("content://media/external/downloads/13417")
        RxHttp.postForm(Url.UPLOAD_URL)
            .addPart(requireContext(), "uploaded_file", uri)
            .upload(AndroidSchedulers.mainThread()) {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress  //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize     //要上传的总字节大小
                tvResult.append("\n" + it.toString())
            }
            .asString()
            .life(this@UploadFragment)   //页面销毁，自动关闭请求
            .subscribe({
                tvResult.append("\n上传成功 : $it")
            }, {
                tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            })
    }


    //-------------------- Android 10之前上传 --------------------

    //协程文件上传，不带进度
    private suspend fun UploadFragmentBinding.coroutineUpload(v: View) {
        RxHttp.postForm(Url.UPLOAD_URL)
            .addFile("uploaded_file", File("xxxx/1.png"))
            .toStr()
            .awaitResult {
                tvResult.append("\n上传成功 : $it")
            }.onFailure {
                tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            }
    }

    //协程上传文件，带进度
    private suspend fun UploadFragmentBinding.coroutineUploadProgress(v: View) {
        RxHttp.postForm(Url.UPLOAD_URL)
            .addFile("uploaded_file", File("xxxx/1.png"))
            .toFlow<String> {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize //要上传的总字节大小
                tvResult.append("\n" + it.toString())
            }.catch {
                tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            }.collect {
                tvResult.append("\n上传成功 : $it")
            }
    }


    //RxJava文件上传，不带进度
    private fun UploadFragmentBinding.rxJavaUpload(v: View) {
        RxHttp.postForm(Url.UPLOAD_URL)
            .addFile("uploaded_file", File("xxxx/1.png"))
            .asString()
            .lifeOnMain(this@UploadFragment)   //页面销毁，自动关闭请求，并在主线程回调
            .subscribe({
                tvResult.append("\n上传成功 : $it")
            }, {
                tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            })
    }

    //RxJava上传文件，带进度
    private fun UploadFragmentBinding.rxJavaUploadProgress(v: View) {
        RxHttp.postForm(Url.UPLOAD_URL)
            .addFile("uploaded_file", File("xxxx/1.png"))
            .upload(AndroidSchedulers.mainThread()) {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress  //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize     //要上传的总字节大小
                tvResult.append("\n" + it.toString())
            }
            .asString()
            .life(this@UploadFragment)   //页面销毁，自动关闭请求
            .subscribe({
                tvResult.append("\n上传成功 : $it")
            }, {
                tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            })
    }

    private fun UploadFragmentBinding.clearLog(view: View) {
        tvResult.text = ""
        tvResult.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onClick(v: View) {
        mBinding.apply {
            when (v.id) {
                R.id.coroutine_upload10 -> lifecycleScope.launch { coroutineUpload10(v) }
                R.id.coroutine_uploadProgress10 ->
                    lifecycleScope.launch { coroutineUploadProgress10(v) }
                R.id.rxjava_upload10 -> rxJavaUpload10(v)
                R.id.rxjava_uploadProgress10 -> rxJavaUploadProgress10(v)
                R.id.coroutine_upload -> lifecycleScope.launch { coroutineUpload(v) }
                R.id.coroutine_uploadProgress ->
                    lifecycleScope.launch { coroutineUploadProgress(v) }
                R.id.rxjava_upload -> rxJavaUpload(v)
                R.id.rxjava_uploadProgress -> rxJavaUploadProgress(v)
                R.id.bt_clear -> clearLog(v)
                else -> {
                }
            }
        }
    }
}