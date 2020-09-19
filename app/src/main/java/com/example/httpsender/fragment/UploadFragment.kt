package com.example.httpsender.fragment

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.rxLifeScope
import com.example.httpsender.R
import com.example.httpsender.databinding.UploadFragmentBinding
import com.example.httpsender.entity.Url
import com.example.httpsender.kt.errorMsg
import com.example.httpsender.kt.show
import com.rxjava.rxlife.RxLife
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import rxhttp.awaitString
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.upload
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
    private fun coroutineUpload10(v: View) {
        rxLifeScope.launch({
            //真实环境，需要调用文件选择器，拿到Uri对象
            val uri = Uri.parse("content://media/external/downloads/13417")
            val result = RxHttp.postForm(Url.UPLOAD_URL)
                .addPart(requireContext(), "uploaded_file", uri)
                .awaitString()
            mBinding.tvResult.append("\n上传成功 : $result")
        }, {
            mBinding.tvResult.append("\n${it.errorMsg}")
            //失败回调
            it.show()
        })
    }

    //协程上传文件，带进度
    private fun coroutineUploadProgress10(v: View) {
        rxLifeScope.launch({
            //真实环境，需要调用文件选择器，拿到Uri对象
            val uri = Uri.parse("content://media/external/downloads/13417")
            val result = RxHttp.postForm(Url.UPLOAD_URL)
                .addPart(requireContext(), "uploaded_file", uri)
                .upload(this) {
                    //上传进度回调,0-100，仅在进度有更新时才会回调
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已上传的字节大小
                    val totalSize = it.totalSize //要上传的总字节大小
                    mBinding.tvResult.append("\n" + it.toString())
                }.awaitString()
            mBinding.tvResult.append("\n上传成功 : $result")
        }, {
            mBinding.tvResult.append("\n${it.errorMsg}")
            //失败回调
            it.show()
        })
    }


    //RxJava文件上传，不带进度
    private fun rxJavaUpload10(v: View) {
        //真实环境，需要调用文件选择器，拿到Uri对象
        val uri = Uri.parse("content://media/external/downloads/13417")
        RxHttp.postForm(Url.UPLOAD_URL)
            .addPart(requireContext(), "uploaded_file", uri)
            .asString()
            .to(RxLife.toMain(this))   //页面销毁，自动关闭请求，并在主线程回调
            .subscribe({
                mBinding.tvResult.append("\n上传成功 : $it")
            }, {
                mBinding.tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            })
    }

    //RxJava上传文件，带进度
    private fun rxJavaUploadProgress10(v: View) {
        //真实环境，需要调用文件选择器，拿到Uri对象
        val uri = Uri.parse("content://media/external/downloads/13417")
        RxHttp.postForm(Url.UPLOAD_URL)
            .addPart(requireContext(), "uploaded_file", uri)
            .upload(AndroidSchedulers.mainThread()) {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress  //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize     //要上传的总字节大小
                mBinding.tvResult.append("\n" + it.toString())
            }
            .asString()
            .to(RxLife.to(this))   //页面销毁，自动关闭请求
            .subscribe({
                mBinding.tvResult.append("\n上传成功 : $it")
            }, {
                mBinding.tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            })
    }


    //-------------------- Android 10之前上传 --------------------

    //协程文件上传，不带进度
    private fun coroutineUpload(v: View) {
        rxLifeScope.launch({
            val result = RxHttp.postForm(Url.UPLOAD_URL)
                .addFile("uploaded_file", File("xxxx/1.png"))
                .awaitString()
            mBinding.tvResult.append("\n上传成功 : $result")
        }, {
            mBinding.tvResult.append("\n${it.errorMsg}")
            //失败回调
            it.show()
        })
    }

    //协程上传文件，带进度
    private fun coroutineUploadProgress(v: View) {
        rxLifeScope.launch({
            val result = RxHttp.postForm(Url.UPLOAD_URL)
                .addFile("uploaded_file", File("xxxx/1.png"))
                .upload(this) {
                    //上传进度回调,0-100，仅在进度有更新时才会回调
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已上传的字节大小
                    val totalSize = it.totalSize //要上传的总字节大小
                    mBinding.tvResult.append("\n" + it.toString())
                }.awaitString()
            mBinding.tvResult.append("\n上传成功 : $result")
        }, {
            mBinding.tvResult.append("\n${it.errorMsg}")
            //失败回调
            it.show()
        })
    }


    //RxJava文件上传，不带进度
    private fun rxJavaUpload(v: View) {
        RxHttp.postForm(Url.UPLOAD_URL)
            .addFile("uploaded_file", File("xxxx/1.png"))
            .asString()
            .to(RxLife.toMain(this))   //页面销毁，自动关闭请求，并在主线程回调
            .subscribe({
                mBinding.tvResult.append("\n上传成功 : $it")
            }, {
                mBinding.tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            })
    }

    //RxJava上传文件，带进度
    private fun rxJavaUploadProgress(v: View) {
        RxHttp.postForm(Url.UPLOAD_URL)
            .addFile("uploaded_file", File("xxxx/1.png"))
            .upload(AndroidSchedulers.mainThread()) {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress  //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize     //要上传的总字节大小
                mBinding.tvResult.append("\n" + it.toString())
            }
            .asString()
            .to(RxLife.to(this))   //页面销毁，自动关闭请求
            .subscribe({
                mBinding.tvResult.append("\n上传成功 : $it")
            }, {
                mBinding.tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            })
    }

    private fun clearLog(view: View) {
        mBinding.tvResult.text = ""
        mBinding.tvResult.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.coroutine_upload10 -> coroutineUpload10(v)
            R.id.coroutine_uploadProgress10 -> coroutineUploadProgress10(v)
            R.id.rxjava_upload10 -> rxJavaUpload10(v)
            R.id.rxjava_uploadProgress10 -> rxJavaUploadProgress10(v)
            R.id.coroutine_upload -> coroutineUpload(v)
            R.id.coroutine_uploadProgress -> coroutineUploadProgress(v)
            R.id.rxjava_upload -> rxJavaUpload(v)
            R.id.rxjava_uploadProgress -> rxJavaUploadProgress(v)
            R.id.bt_clear -> clearLog(v)
        }
    }
}