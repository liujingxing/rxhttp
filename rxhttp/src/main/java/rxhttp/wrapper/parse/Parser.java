package rxhttp.wrapper.parse;

import java.io.IOException;

import io.reactivex.annotations.NonNull;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rxhttp.wrapper.exception.ExceptionHelper;
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
     * @param response Http响应
     * @return 根据Response获取最终结果
     * @throws IOException 请求失败异常、网络不可用异常、空异常
     */
    @NonNull
    default String getResult(@NonNull Response response) throws IOException {
        ResponseBody body = ExceptionHelper.throwIfFatal(response);
        String result = body.string();
        LogUtil.log(response, result);
        return result;
    }
}
