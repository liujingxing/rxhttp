package rxhttp.wrapper.parse;

import java.io.IOException;
import java.lang.reflect.Type;

import io.reactivex.annotations.NonNull;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.exception.ExceptionHelper;
import rxhttp.wrapper.param.Param;
import rxhttp.wrapper.utils.LogUtil;

/**
 * 数据解析接口
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
public interface Parser<T> {

    /**
     * 数据解析,Http请求成功后回调
     *
     * @param response Http执行结果
     * @return 解析后的对象类型
     * @throws IOException 网络异常、解析异常
     */
    T onParse(@NonNull Response response) throws IOException;


    /**
     * 此方法将在未来版本中删除
     *
     * @param response Http响应
     * @return 根据Response获取最终结果
     * @throws IOException 请求失败异常、网络不可用异常、空异常
     */
    @Deprecated
    @NonNull
    default String getResult(@NonNull Response response) throws IOException {
        ResponseBody body = ExceptionHelper.throwIfFatal(response);
        boolean onResultDecoder = isOnResultDecoder(response);
        LogUtil.log(response, onResultDecoder, null);
        String result = body.string();
        return onResultDecoder ? RxHttpPlugins.onResultDecoder(result) : result;
    }

    default <R> R convert(Response response, Type type) throws IOException {
        ResponseBody body = ExceptionHelper.throwIfFatal(response);
        boolean onResultDecoder = isOnResultDecoder(response);
        LogUtil.log(response, onResultDecoder, null);
        IConverter converter = getConverter(response);
        return converter.convert(body, type, onResultDecoder);
    }

    default boolean isOnResultDecoder(Response response) {
        return !"false".equals(response.request().header(Param.DATA_DECRYPT));
    }

    default IConverter getConverter(Response response) {
        return response.request().tag(IConverter.class);
    }
}
