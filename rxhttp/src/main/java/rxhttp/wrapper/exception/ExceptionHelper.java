package rxhttp.wrapper.exception;


import java.io.IOException;

import io.reactivex.annotations.NonNull;
import okhttp3.Response;

/**
 * 异常处理帮助类
 * User: ljx
 * Date: 2018/11/21
 * Time: 09:30
 */
public class ExceptionHelper {

    /**
     * 根据Http执行结果过滤异常
     *
     * @param response Http响应体
     * @param result   Http执行结果
     * @throws IOException 请求失败异常、网络不可用异常
     */
    public static void throwIfFatal(@NonNull Response response, String result) throws IOException {
        if (!response.isSuccessful()) throw new IOException(response.message());
        if (("<html>\n" +
                "<meta http-equiv=\"pragma\" content=\"no-cache\">\n" +
                "<meta http-equiv=\"Cache-Control\" content=\"no-cache,must-revalidate\">\n" +
                "<meta http-equiv=\"expires\" content=\"0\">\n" +
                "<head>\n" +
                "<script>\n" +
                "    window.location.href='/login?has_ori_uri';\n" +
                "</script>\n" +
                "</head>\n" +
                "</html>\n").equals(result)) {
            //此判断语句不准确，请忽略
            throw new NetworkErrorException("Network unavailable");
        }
    }
}
