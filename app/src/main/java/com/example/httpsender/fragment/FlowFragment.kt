package com.example.httpsender.fragment

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.httpsender.R
import com.example.httpsender.databinding.FlowFragmentBinding
import com.example.httpsender.entity.Article
import com.example.httpsender.entity.Location
import com.example.httpsender.entity.Name
import com.example.httpsender.entity.NewsDataXml
import com.example.httpsender.entity.PageList
import com.example.httpsender.entity.Url
import com.example.httpsender.kt.errorMsg
import com.example.httpsender.kt.show
import com.example.httpsender.parser.Android10DownloadFactory
import com.google.gson.Gson
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import rxhttp.toFlow
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toFlowResponse
import java.io.File

/**
 * 使用 协程(RxHttp + Flow) 发请求
 *
 * ```
 * RxHttp.postXxx("/service/...")
 *     .add("key", "value")
 *     .toFlow<User>()
 *     .catch {
 *        val throwable = it
 *     }.collect {
 *        val user = it
 *     }
 *```
 * User: ljx
 * Date: 2021/9/18
 * Time: 20:16
 */
class FlowFragment : BaseFragment<FlowFragmentBinding>(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flow_fragment)
    }

    override fun FlowFragmentBinding.onViewCreated(savedInstanceState: Bundle?) {
        click = this@FlowFragment
    }

    //发送Get请求，获取文章列表
    private suspend fun FlowFragmentBinding.sendGet(view: View) {
        RxHttp.get("/article/list/0/json")
            .toFlowResponse<PageList<Article>>()
            .catch {
                tvResult.text = it.errorMsg
                it.show()
            }.collect {
                tvResult.text = Gson().toJson(it)
            }
    }

    //发送Post表单请求,根据关键字查询文章
    private suspend fun FlowFragmentBinding.sendPostForm(view: View) {
        RxHttp.postForm("/article/query/0/json")
            .add("k", "性能优化")
            .toFlowResponse<PageList<Article>>()
            .catch {
                tvResult.text = it.errorMsg
                it.show()
            }.collect {
                tvResult.text = Gson().toJson(it)
            }
    }

    //发送Post Json请求，此接口不通，通过日志可以看到，发送出去的json对象
    private suspend fun FlowFragmentBinding.sendPostJson(view: View) {
        /*
           发送以下User对象
           {"name":"张三","sex":1,"height":180,"weight":70,
           "interest":["羽毛球","游泳"],
           "location":{"latitude":30.7866,"longitude":120.6788},
           "address":{"street":"科技园路.","city":"江苏苏州","country":"中国"}}
         */
        val interestList: MutableList<String> = ArrayList() //爱好
        interestList.add("羽毛球")
        interestList.add("游泳")
        val address = """
            {"street":"科技园路.","city":"江苏苏州","country":"中国"}
        """.trimIndent()
        RxHttp.postJson("/article/list/0/json")
            .add("name", "张三")
            .add("sex", 1)
            .addAll("""{"height":180,"weight":70}""") //通过addAll系列方法添加多个参数
            .add("interest", interestList) //添加数组对象
            .add("location", Location(120.6788, 30.7866)) //添加位置对象
            .addJsonElement("address", address) //通过字符串添加一个对象
            .toFlow<String>()
            .catch {
                tvResult.text = it.errorMsg
                it.show()
            }.collect {
                tvResult.text = it
            }
    }

    //发送Post JsonArray请求，通过日志可以看到，发送出去的json数组
    private suspend fun FlowFragmentBinding.sendPostJsonArray(view: View) {
        /*
           发送以下Json数组
           [{"name":"张三"},{"name":"李四"},{"name":"王五"},{"name":"赵六"},{"name":"杨七"}]
         */
        val names: MutableList<Name?> = ArrayList()
        names.add(Name("赵六"))
        names.add(Name("杨七"))
        RxHttp.postJsonArray("/article/list/0/json")
            .add("name", "张三")
            .add(Name("李四"))
            .addJsonElement("""{"name":"王五"}""")
            .addAll(names)
            .toFlow<String>()
            .catch {
                tvResult.text = it.errorMsg
                it.show()
            }.collect {
                tvResult.text = it
            }

    }

    //此接口不同，但通过日志可以看到，发送出去的是xml数据，如果收到也是xml数据，则会自动解析为我们指定的对象
    private suspend fun FlowFragmentBinding.xmlConverter(view: View) {
        RxHttp.postBody("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni")
            .setBody(Name("张三"))
            .setXmlConverter()
            .toFlow<NewsDataXml>()
            .catch {
                tvResult.text = it.errorMsg
                it.show()
            }.collect {
                tvResult.text = Gson().toJson(it)
            }
    }

    /**
     * android 10之前 或 沙盒目录(Android/data/packageName/)下的文件上传
     *
     * 如不需要监听进度，toFlow 方法不要传进度回调即可
     */
    private suspend fun FlowFragmentBinding.upload(v: View) {
        RxHttp.postForm(Url.UPLOAD_URL)
            .addFile("file", File("xxxx/1.png"))
            .toFlow<String> {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize //要上传的总字节大小
                tvResult.append(it.toString())
            }.catch {
                tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            }.collect {
                tvResult.append("\n上传成功 : $it")
            }
    }

    /**
     * android 10 及以上文件上传 ，兼容Android 10以下
     *
     * 如不需要监听进度，toFlow 方法不要传进度回调即可
     */
    private suspend fun FlowFragmentBinding.uploadAndroid10(v: View) {
        //真实环境，需要调用文件选择器，拿到Uri对象
        val uri = Uri.parse("content://media/external/downloads/13417")
        RxHttp.postForm(Url.UPLOAD_URL)
            .addPart(requireContext(), "file", uri)
            .toFlow<String> {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已上传的字节大小
                val totalSize = it.totalSize //要上传的总字节大小
                tvResult.append(it.toString())
            }.catch {
                tvResult.append("\n${it.errorMsg}")
                //失败回调
                it.show()
            }.collect {
                tvResult.append("\n上传成功 : $it")
            }
    }

    /**
     * Android 10以下 或 下载文件到沙盒目录下，下载可以直接传入file的绝对路径
     *
     * 如不需要监听下载进度，toFlow 方法不要传进度回调即可
     */
    private suspend fun FlowFragmentBinding.download(view: View) {
        val destPath = "${requireContext().externalCacheDir}/${System.currentTimeMillis()}.apk"
        RxHttp.get(Url.DOWNLOAD_URL)
            .toFlow(destPath) {
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }.catch {
                tvResult.append("\n${it.errorMsg}") //异常回调
                it.show()
            }.collect {
                tvResult.append("\n下载完成, $it")
            }
    }

    /**
     * 断点下载
     * Android 10以下 或 下载文件到沙盒目录下，下载可以直接传入file的绝对路径
     *
     * 如不需要监听下载进度，toFlow 方法不要传进度回调即可
     */
    private suspend fun FlowFragmentBinding.appendDownload(view: View) {
        val destPath = "${requireContext().externalCacheDir}/Miaobo.apk"
        RxHttp.get(Url.DOWNLOAD_URL)
            .toFlow(destPath, true) {
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }.catch {
                tvResult.append("\n${it.errorMsg}")
                it.show()
            }.collect {
                tvResult.append("\n下载完成, $it")
            }
    }

    /**
     * Android 10 及以上下载，兼容Android 10以下
     *
     * 如不需要监听下载进度，toFlow 方法不要传进度回调即可
     */
    private suspend fun FlowFragmentBinding.downloadAndroid10(view: View) {
        val factory = Android10DownloadFactory(requireContext(), "miaobo.apk")
        RxHttp.get(Url.DOWNLOAD_URL)
            .toFlow(factory) {
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }.catch {
                tvResult.append("\n${it.errorMsg}")
                it.show()
            }.collect {
                tvResult.append("\n下载完成, $it")
            }
    }

    /**
     * Android 10 及以上断点下载，兼容Android 10以下
     *
     * 如不需要监听下载进度，toFlow 方法不要传进度回调即可
     */
    private suspend fun FlowFragmentBinding.appendDownloadAndroid10(view: View) {
        val factory = Android10DownloadFactory(requireContext(), "miaobo.apk")
        RxHttp.get(Url.DOWNLOAD_URL)
            .toFlow(factory, true) {
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                tvResult.append(it.toString())
            }.catch {
                //异常回调
                tvResult.append("\n${it.errorMsg}")
                it.show()
            }.collect {
                tvResult.append("\n下载完成, $it")
            }
    }


    private fun FlowFragmentBinding.clearLog(view: View) {
        tvResult.text = ""
        tvResult.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onClick(v: View) {
        mBinding.run {
            lifecycleScope.launch {
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
}