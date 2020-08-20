package com.example.httpsender.parser

import android.content.Context
import android.net.Uri
import okhttp3.Response
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.utils.IOUtil
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException

/**
 * 文件下载解析器
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
//@rxhttp.wrapper.annotation.Parser(name = "Download10")
class Download10Parser(
    private val context: Context,
    private val uri: Uri
) : Parser<String> {

    /**
     * @param response Http请求执行结果
     * @return 下载成功后的文件路径
     * @throws IOException 网络异常等,RxJava的观察者会捕获此异常
     */
    @Throws(IOException::class)
    override fun onParse(response: Response): String {
        val body = ExceptionHelper.throwIfFatal(response)
        LogUtil.log(response, false, uri.toString())
        val append = response.header("Content-Range") != null
        val outputStream = context.contentResolver.run {
            openOutputStream(uri, if (append) "wa" else "w")
        }
        IOUtil.write(body.byteStream(), outputStream) //将输入流写出到文件
        return uri.toString()
    }

}