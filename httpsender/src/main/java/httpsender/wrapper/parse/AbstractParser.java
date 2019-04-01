package httpsender.wrapper.parse;


import com.google.gson.internal.$Gson$Preconditions;
import com.google.gson.internal.$Gson$Types;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import httpsender.wrapper.exception.ExceptionHelper;
import httpsender.wrapper.utils.LogUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * User: ljx
 * Date: 2019/1/21
 * Time: 15:32
 */
public abstract class AbstractParser<T> implements Parser<T> {

    protected Type mType;

    public AbstractParser() {
        mType = getActualTypeParameter();
    }

    public AbstractParser(Type type) {
        mType = $Gson$Types.canonicalize($Gson$Preconditions.checkNotNull(type));
    }

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
        LogUtil.log(response, result);
        ExceptionHelper.throwIfFatal(response, result);
        return result;
    }

    /**
     * @return 当前类超类的第一个泛型参数类型
     */
    private Type getActualTypeParameter() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameter = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameter.getActualTypeArguments()[0]);
    }
}
