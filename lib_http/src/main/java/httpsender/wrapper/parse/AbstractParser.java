package httpsender.wrapper.parse;

import android.util.Log;

import com.google.gson.internal.$Gson$Types;
import com.network.http.BuildConfig;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import httpsender.wrapper.exception.ExceptionHelper;
import io.reactivex.annotations.NonNull;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * User: ljx
 * Date: 2019/1/21
 * Time: 15:32
 */
public abstract class AbstractParser<T> implements Parser<T> {

    /**
     * @param response Http响应
     * @return 根据Response获取最终结果
     * @throws IOException 请求失败异常、网络不可用异常、空异常
     */
    @NonNull
    protected final String getResult(@NonNull Response response) throws IOException {
        ResponseBody body = response.body();
        if (body == null) throw new IOException("ResponseBody is null");
        String result = body.string();
        log(response, result);
        ExceptionHelper.throwIfFatal(response, result);
        return result;
    }

    /**
     * @return 当前类超类的第一个泛型参数类型
     */
    protected final Type getActualTypeParameter() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameter = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameter.getActualTypeArguments()[0]);
    }

    //打印日志
    private void log(@NonNull Response response, String result) {
        if (BuildConfig.DEBUG) {
            Request request = response.request();
            String builder = "-------------------Method=" +
                    request.method() + " Code=" + response.code() + "-------------------" +
                    "\nUrl=" + request.url() +
                    "\nResult=" + result;
            Log.w("HttpSender", builder);
        }
    }
}
