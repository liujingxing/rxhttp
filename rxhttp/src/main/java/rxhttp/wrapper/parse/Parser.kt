package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.callback.IConverter
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.param.Param
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import java.lang.reflect.Type

/**
 * 数据解析接口
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
interface Parser<T> {
    /**
     * 数据解析,Http请求成功后回调
     *
     * @param response Http执行结果
     * @return 解析后的对象类型
     * @throws IOException 网络异常、解析异常
     */
    @Throws(IOException::class)
    fun onParse(response: Response): T

    /**
     * 此方法将在未来版本中删除
     *
     * @param response Http响应
     * @return 根据Response获取最终结果
     * @throws IOException 请求失败异常、网络不可用异常、空异常
     */
    @Deprecated("")
    @Throws(IOException::class)
    fun getResult(response: Response): String {
        val body = ExceptionHelper.throwIfFatal(response)
        val onResultDecoder = isOnResultDecoder(response)
        LogUtil.log(response, onResultDecoder, null)
        val result = body.string()
        return if (onResultDecoder) RxHttpPlugins.onResultDecoder(result) else result
    }

    @Throws(IOException::class)
    fun <R> convert(response: Response, type: Type): R {
        val body = ExceptionHelper.throwIfFatal(response)
        val onResultDecoder = isOnResultDecoder(response)
        LogUtil.log(response, onResultDecoder, null)
        val converter = getConverter(response)
        return converter!!.convert(body, type, onResultDecoder)
    }

    fun isOnResultDecoder(response: Response): Boolean {
        return "false" != OkHttpCompat.request(response).header(Param.DATA_DECRYPT)
    }

    fun getConverter(response: Response): IConverter? {
        return OkHttpCompat.request(response).tag(IConverter::class.java)
    }
}