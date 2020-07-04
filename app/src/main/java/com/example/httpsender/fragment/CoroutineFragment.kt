package com.example.httpsender.fragment

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.rxLifeScope
import com.example.httpsender.DownloadMultiActivity
import com.example.httpsender.R
import com.example.httpsender.databinding.CoroutineFragmentBinding
import com.example.httpsender.entity.*
import com.example.httpsender.kt.errorMsg
import com.example.httpsender.kt.show
import com.example.httpsender.kt.startActivity
import com.google.gson.Gson
import rxhttp.toBitmap
import rxhttp.toClass
import rxhttp.toDownload
import rxhttp.toStr
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toResponse
import rxhttp.wrapper.param.upload
import java.io.File
import java.util.*

/**
 * 使用Coroutine+OkHttp发请求
 * User: ljx
 * Date: 2020/4/24
 * Time: 18:16
 */
class CoroutineFragment : Fragment(), View.OnClickListener {

    private lateinit var mBinding: CoroutineFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.coroutine_fragment, container, false)
        mBinding.click = this
        return mBinding.root
    }

    private fun bitmap(view: View) = rxLifeScope.launch({
        val imageUrl = "http://img2.shelinkme.cn/d3/photos/0/017/022/755_org.jpg@!normal_400_400?1558517697888"
        val bitmap = RxHttp.get(imageUrl).toBitmap().await()
        mBinding.tvResult.background = BitmapDrawable(bitmap)
    }, {
        mBinding.tvResult.text = it.errorMsg
        //失败回调
        it.show("图片加载失败,请稍后再试!")
    })

    //发送Get请求，获取文章列表
    private fun sendGet(view: View) = rxLifeScope.launch({
        val pageList = RxHttp.get("/article/list/0/json")
            .setSimpleClient()
            .toResponse<PageList<Article>>()
            .await()
        mBinding.tvResult.text = Gson().toJson(pageList)
    }, {
        mBinding.tvResult.text = it.errorMsg
        //失败回调
        it.show("发送失败,请稍后再试!")
    })

    //发送Post表单请求,根据关键字查询文章
    private fun sendPostForm(view: View) = rxLifeScope.launch({
        val pageList = RxHttp.postForm("/article/query/0/json")
            .add("k", "性能优化")
            .toResponse<PageList<Article>>()
            .await()
        mBinding.tvResult.text = Gson().toJson(pageList)
    }, {
        mBinding.tvResult.text = it.errorMsg
        //失败回调
        it.show("发送失败,请稍后再试!")
    })

    //发送Post Json请求，此接口不通，仅用于调试参数
    private fun sendPostJson(view: View) = rxLifeScope.launch({
        //发送以下User对象
        /*
           {
               "name": "张三",
               "sex": 1,
               "height": 180,
               "weight": 70,
               "interest": [
                   "羽毛球",
                   "游泳"
               ],
               "location": {
                   "latitude": 30.7866,
                   "longitude": 120.6788
               },
               "address": {
                   "street": "科技园路.",
                   "city": "江苏苏州",
                   "country": "中国"
               }
           }
         */
        val interestList: MutableList<String> = ArrayList() //爱好
        interestList.add("羽毛球")
        interestList.add("游泳")
        val address = "{\"street\":\"科技园路.\",\"city\":\"江苏苏州\",\"country\":\"中国\"}"
        val result = RxHttp.postJson("/article/list/0/json")
            .add("name", "张三")
            .add("sex", 1)
            .addAll("{\"height\":180,\"weight\":70}") //通过addAll系列方法添加多个参数
            .add("interest", interestList) //添加数组对象
            .add("location", Location(120.6788, 30.7866)) //添加位置对象
            .addJsonElement("address", address) //通过字符串添加一个对象
            .toStr()
            .await()
        mBinding.tvResult.text = result
    }, {
        mBinding.tvResult.text = it.errorMsg
        //失败回调
        it.show("发送失败,请稍后再试!")
    })

    //发送Post JsonArray请求，此接口不通，仅用于调试参数
    private fun sendPostJsonArray(view: View) = rxLifeScope.launch({
        //发送以下Json数组
        /*
           [
               {
                   "name": "张三"
               },
               {
                   "name": "李四"
               },
               {
                   "name": "王五"
               },
               {
                   "name": "赵六"
               },
               {
                   "name": "杨七"
               }
           ]
         */
        val names: MutableList<Name?> = ArrayList()
        names.add(Name("赵六"))
        names.add(Name("杨七"))
        val result = RxHttp.postJsonArray("/article/list/0/json")
            .add("name", "张三")
            .add(Name("李四"))
            .addJsonElement("{\"name\":\"王五\"}")
            .addAll(names)
            .toStr()
            .await()
        mBinding.tvResult.text = result
    }, {
        mBinding.tvResult.text = it.errorMsg
        //失败回调
        it.show("发送失败,请稍后再试!")
    })

    //使用XmlConverter解析数据，此接口返回数据太多，会有点慢
    private fun xmlConverter(view: View) = rxLifeScope.launch({
        val dataXml = RxHttp.get("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni")
            .setXmlConverter()
            .toClass<NewsDataXml>()
            .await()
        mBinding.tvResult.text = Gson().toJson(dataXml)
    }, {
        mBinding.tvResult.text = it.errorMsg
        //失败回调
        it.show("发送失败,请稍后再试!")
    })

    //使用XmlConverter解析数据
    private fun fastJsonConverter(view: View) = rxLifeScope.launch({
        val pageList = RxHttp.get("/article/list/0/json")
            .setFastJsonConverter()
            .toResponse<PageList<Article>>()
            .await()
        mBinding.tvResult.text = Gson().toJson(pageList)
    }, {
        mBinding.tvResult.text = it.errorMsg
        //失败回调
        it.show("发送失败,请稍后再试!")
    })

    //文件下载，不带进度
    private fun download(view: View) = rxLifeScope.launch({
        val destPath = requireContext().externalCacheDir.toString() + "/" + System.currentTimeMillis() + ".apk"
        val result = RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .toDownload(destPath)
            .await()
        mBinding.tvResult.text = "下载完成,路径$result"
    }, {
        mBinding.tvResult.text = it.errorMsg
        //失败回调
        it.show("发送失败,请稍后再试!")
    })

    //文件下载，带进度
    private fun downloadAndProgress(view: View) = rxLifeScope.launch({
        //文件存储路径
        val destPath = requireContext().externalCacheDir.toString() + "/" + System.currentTimeMillis() + ".apk"
        val result = RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .toDownload(destPath, this) {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                mBinding.tvResult.append(it.toString())
            }
            .await()
        mBinding.tvResult.append(result)
    }, {
        mBinding.tvResult.append(it.errorMsg)
        //失败回调
        it.show("发送失败,请稍后再试!")
    })

    //断点下载
    private fun breakpointDownload(view: View) = rxLifeScope.launch({
        val destPath = requireContext().externalCacheDir.toString() + "/" + "Miaobo.apk"
        val length = File(destPath).length()
        val result = RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .setRangeHeader(length) //设置开始下载位置，结束位置默认为文件末尾
            .toDownload(destPath) //注意这里使用DownloadParser解析器，并传入本地路径
            .await()
        mBinding.tvResult.text = "下载完成,路径$result"
    }, {
        mBinding.tvResult.append(it.errorMsg)
        //失败回调
        it.show("发送失败,请稍后再试!")
    })

    //断点下载，带进度
    private fun breakpointDownloadAndProgress(view: View) = rxLifeScope.launch({
        val destPath = requireContext().externalCacheDir.toString() + "/" + "Miaobo.apk"
        val length = File(destPath).length()
        val result = RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .setRangeHeader(length, true) //设置开始下载位置，结束位置默认为文件末尾
            .toDownload(destPath, this) {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                mBinding.tvResult.append(it.toString())
            }
            .await()
        mBinding.tvResult.append(result)
    }, {
        mBinding.tvResult.append(it.errorMsg)
        //失败回调
        it.show("发送失败,请稍后再试!")
    })

    //文件上传，不带进度
    private fun upload(v: View) = rxLifeScope.launch({
        val result = RxHttp.postForm("http://t.xinhuo.com/index.php/Api/Pic/uploadPic")
            .addFile("uploaded_file", File(Environment.getExternalStorageDirectory(), "1.jpg"))
            .toStr()
            .await()
        mBinding.tvResult.append("\n")
        mBinding.tvResult.append(result)
    }, {
        mBinding.tvResult.append("\n")
        mBinding.tvResult.append(it.errorMsg)
        //失败回调
        it.show("上传失败,请稍后再试!")
    })

    //上传文件，带进度
    private fun uploadAndProgress(v: View) = rxLifeScope.launch({
        val result = RxHttp.postForm("http://t.xinhuo.com/index.php/Api/Pic/uploadPic")
            .addFile("uploaded_file", File(Environment.getExternalStorageDirectory(), "1.jpg"))
            .upload(this) {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize //要上传的总字节大小
                mBinding.tvResult.append("\n" + it.toString())
            }
            .toStr()
            .await()
        mBinding.tvResult.append("\n上传成功 : $result")
    }, {
        mBinding.tvResult.append("\n")
        mBinding.tvResult.append(it.errorMsg)
        //失败回调
        it.show("上传失败,请稍后再试!")
    })

    private fun clearLog(view: View) {
        mBinding.tvResult.text = ""
        mBinding.tvResult.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bitmap -> bitmap(v)
            R.id.sendGet -> sendGet(v)
            R.id.sendPostForm -> sendPostForm(v)
            R.id.sendPostJson -> sendPostJson(v)
            R.id.sendPostJsonArray -> sendPostJsonArray(v)
            R.id.xmlConverter -> xmlConverter(v)
            R.id.fastJsonConverter -> fastJsonConverter(v)
            R.id.download -> download(v)
            R.id.downloadAndProgress -> downloadAndProgress(v)
            R.id.breakpointDownload -> breakpointDownload(v)
            R.id.breakpointDownloadAndProgress -> breakpointDownloadAndProgress(v)
            R.id.upload -> upload(v)
            R.id.uploadAndProgress -> uploadAndProgress(v)
            R.id.multitaskDownload -> startActivity(DownloadMultiActivity::class)
            R.id.bt_clear -> clearLog(v)
        }
    }
}