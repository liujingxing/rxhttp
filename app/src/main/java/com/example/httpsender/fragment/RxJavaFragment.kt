package com.example.httpsender.fragment

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.example.httpsender.R
import com.example.httpsender.databinding.RxjavaFragmentBinding
import com.example.httpsender.entity.*
import com.example.httpsender.kt.errorMsg
import com.example.httpsender.kt.show
import com.example.httpsender.parser.Android10DownloadFactory
import com.google.gson.Gson
import com.rxjava.rxlife.life
import com.rxjava.rxlife.lifeOnMain
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.asClass
import rxhttp.wrapper.param.asResponse
import java.io.File
import java.util.*

/**
 * 使用 RxHttp + RxJava 发请求
 *
 * ```
 * RxHttp.postXxx("/service/...")
 *     .add("key", "value")
 *     .asClass<User>()
 *     .subscribe(user ->{
 *
 *     }, throwable -> {
 *
 *     })
 * ```
 * User: ljx
 * Date: 2020/4/24
 * Time: 18:16
 */
class RxJavaFragment : BaseFragment<RxjavaFragmentBinding>(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rxjava_fragment)
    }

    override fun RxjavaFragmentBinding.onViewCreated(savedInstanceState: Bundle?) {
        click = this@RxJavaFragment
    }

    //发送Get请求，获取文章列表
    fun RxjavaFragmentBinding.sendGet(view: View?) {
        RxHttp.get("/article/list/0/json")
            .asResponse<PageList<Article>>()
            .lifeOnMain(this@RxJavaFragment)
            .subscribe({
                tvResult.text = Gson().toJson(it)
            }, {
                tvResult.text = it.errorMsg
                it.show()
            })
    }

    //发送Post表单请求,根据关键字查询文章
    fun RxjavaFragmentBinding.sendPostForm(view: View?) {
        RxHttp.postForm("/article/query/0/json")
            .add("k", "性能优化")
            .asResponse<PageList<Article>>()
            .lifeOnMain(this@RxJavaFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.text = Gson().toJson(it)
            }, {
                tvResult.text = it.errorMsg
                it.show()
            })
    }

    //发送Post Json请求，此接口不通，通过日志可以看到，发送出去的json对象
    fun RxjavaFragmentBinding.sendPostJson(view: View?) {
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
        RxHttp.postJson("/article/list/0/json")
            .add("name", "张三")
            .add("sex", 1)
            .addAll("{\"height\":180,\"weight\":70}") //通过addAll系列方法添加多个参数
            .add("interest", interestList) //添加数组对象
            .add("location", Location(120.6788, 30.7866)) //添加位置对象
            .addJsonElement("address", address) //通过字符串添加一个对象
            .asString()
            .lifeOnMain(this@RxJavaFragment)//感知生命周期，并在主线程回调
            .subscribe({
                tvResult.text = it
            }, {
                tvResult.text = it.errorMsg
                it.show()
            })
    }

    //发送Post JsonArray请求，通过日志可以看到，发送出去的json数组
    fun RxjavaFragmentBinding.sendPostJsonArray(view: View?) {
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
        RxHttp.postJsonArray("/article/list/0/json")
            .add("name", "张三")
            .add(Name("李四"))
            .addJsonElement("{\"name\":\"王五\"}")
            .addAll(names)
            .asString()
            .lifeOnMain(this@RxJavaFragment)
            .subscribe({
                tvResult.text = it
            }, {
                tvResult.text = it.errorMsg
                //失败回调
                it.show()
            })
    }

    //此接口不同，但通过日志可以看到，发送出去的是xml数据，如果收到也是xml数据，则会自动解析为我们指定的对象
    fun RxjavaFragmentBinding.xmlConverter(view: View?) {
        RxHttp.postBody("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni")
            .setBody(Name("张三"))
            .setXmlConverter()
            .asClass<NewsDataXml>()
            .lifeOnMain(this@RxJavaFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.text = Gson().toJson(it)
            }, {
                tvResult.text = it.errorMsg
                //失败回调
                it.show()
            })
    }

    /**
     * android 10之前 或 沙盒目录(Android/data/packageName/)下的文件上传，如不需要监听进度，注释掉 upload 方法即可
     */
    private fun RxjavaFragmentBinding.upload(v: View) {
        RxHttp.postForm(Url.UPLOAD_URL)
            .addFile("file", File("xxxx/1.png"))
            .upload(AndroidSchedulers.mainThread()) {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress  //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize     //要上传的总字节大小
                tvResult.append(it.toString())
            }
            .asString()
            .life(this@RxJavaFragment)    //页面销毁，自动关闭请求
            .subscribe({
                tvResult.append("\n上传成功 : $it")
            }, {
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    /**
     * android 10 及以上文件上传 ，兼容Android 10以下，如不需要监听进度，注释掉 upload 方法即可
     */
    private fun RxjavaFragmentBinding.uploadAndroid10(v: View) {
        //真实环境，需要调用文件选择器，拿到Uri对象
        val uri = Uri.parse("content://media/external/downloads/13417")
        RxHttp.postForm(Url.UPLOAD_URL)
            .addPart(requireContext(), "file", uri)
            .upload(AndroidSchedulers.mainThread()) {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress  //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize     //要上传的总字节大小
                tvResult.append(it.toString())
            }
            .asString()
            .life(this@RxJavaFragment)   //页面销毁，自动关闭请求
            .subscribe({
                tvResult.append("\n上传成功 : $it")
            }, {
                tvResult.append("\n${it.errorMsg}")  //失败回调
                it.show()
            })
    }


    /**
     * Android 10以下 或 下载文件到沙盒目录下，下载可以直接传入file的绝对路径
     *
     * 如不需要监听下载进度，asDownload 方法不要传进度回调即可
     */
    private fun RxjavaFragmentBinding.download(view: View) {
        val destPath = "${requireContext().externalCacheDir}/${System.currentTimeMillis()}.apk"
        RxHttp.get(Url.DOWNLOAD_URL)
            .asDownload(destPath, AndroidSchedulers.mainThread()) {
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }
            .life(this@RxJavaFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    /**
     * 断点下载
     * Android 10以下 或 下载文件到沙盒目录下，下载可以直接传入file的绝对路径
     * 如不需要监听下载进度，asAppendDownload 方法不要传进度回调即可
     */
    private fun RxjavaFragmentBinding.appendDownload(view: View) {
        val destPath = "${requireContext().externalCacheDir}/Miaobo.apk"
        RxHttp.get(Url.DOWNLOAD_URL)
            .asAppendDownload(destPath, AndroidSchedulers.mainThread()) {
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }
            .life(this@RxJavaFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }


    /**
     * Android 10 及以上下载，兼容Android 10以下
     * 如不需要监听下载进度，asDownload 方法不要传进度回调即可
     */
    private fun RxjavaFragmentBinding.downloadAndroid10(view: View) {
        val factory = Android10DownloadFactory(requireContext(), "miaobo.apk")
        RxHttp.get(Url.DOWNLOAD_URL)
            .asDownload(factory, AndroidSchedulers.mainThread()) {
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }
            .life(this@RxJavaFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    /**
     * Android 10 及以上断点下载，兼容Android 10以下
     * 如不需要监听下载进度，asAppendDownload 方法不要传进度回调即可
     */
    private fun RxjavaFragmentBinding.appendDownloadAndroid10(view: View) {
        val factory = Android10DownloadFactory(requireContext(), "miaobo.apk")
        RxHttp.get(Url.DOWNLOAD_URL)
            .asAppendDownload(factory, AndroidSchedulers.mainThread()) {
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }
            .life(this@RxJavaFragment) //感知生命周期，并在主线程回调
            .subscribe({
                tvResult.append("\n下载完成, $it")
            }, {
                //下载失败
                tvResult.append("\n${it.errorMsg}")
                it.show()
            })
    }

    private fun RxjavaFragmentBinding.clearLog(view: View?) {
        tvResult.text = ""
        tvResult.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onClick(v: View) {
        mBinding.apply {
            when (v.id) {
                R.id.sendGet -> sendGet(v)
                R.id.sendPostForm -> sendPostForm(v)
                R.id.sendPostJson -> sendPostJson(v)
                R.id.sendPostJsonArray -> sendPostJsonArray(v)
                R.id.xmlConverter -> xmlConverter(v)
                R.id.upload -> upload(v)
                R.id.upload10 -> uploadAndroid10(v)
                R.id.download -> download(v)
                R.id.download_append -> appendDownload(v)
                R.id.download10 -> downloadAndroid10(v)
                R.id.download10_append -> appendDownloadAndroid10(v)
                R.id.bt_clear -> clearLog(v)
            }
        }
    }
}