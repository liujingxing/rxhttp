package rxhttp.wrapper.parse;

import java.io.IOException;

import io.reactivex.annotations.NonNull;
import okhttp3.Response;

/**
 * 数据解析接口
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
public interface Parser<T> {

    /**
     * 数据解析
     * @param response Http执行结果
     * @return 解析后的对象类型
     * @throws IOException 网络异常、解析异常
     */
    T onParse(@NonNull Response response) throws IOException;

}
