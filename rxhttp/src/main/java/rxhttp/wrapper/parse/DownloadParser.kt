package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.IOUtil
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException

/**
 * 文件下载解析器
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
class DownloadParser(
    private val mDestPath: String   //destPath 目标路径
) : Parser<String> {

    /**
     * @param response Http请求执行结果
     * @return 下载成功后的文件路径
     * @throws IOException 网络异常等,RxJava的观察者会捕获此异常
     */
    @Throws(IOException::class)
    override fun onParse(response: Response): String {
        val body = ExceptionHelper.throwIfFatal(response)
        LogUtil.log(response, false, mDestPath)
        val append = OkHttpCompat.header(response, "Content-Range") != null
        IOUtil.write(body.byteStream(), mDestPath, append) //将输入流写出到文件
        return mDestPath
    }

}