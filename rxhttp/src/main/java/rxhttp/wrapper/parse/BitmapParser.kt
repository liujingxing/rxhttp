package rxhttp.wrapper.parse

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.Response
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException

/**
 * Bitmap解析器
 * User: ljx
 * Date: 2019/06/07
 * Time: 14:35
 */
class BitmapParser : Parser<Bitmap> {
    /**
     * @param response Http请求执行结果
     * @return Bitmap 对象
     * @throws IOException 网络异常等,RxJava的观察者会捕获此异常
     */
    @Throws(IOException::class)
    override fun onParse(response: Response): Bitmap {
        val body = ExceptionHelper.throwIfFatal(response)
        body.use {
            LogUtil.log(response, null)
            return BitmapFactory.decodeStream(it.byteStream())
        }
    }
}