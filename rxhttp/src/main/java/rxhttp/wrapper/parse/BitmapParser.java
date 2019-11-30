package rxhttp.wrapper.parse;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;
import rxhttp.wrapper.exception.ExceptionHelper;
import rxhttp.wrapper.utils.LogUtil;

/**
 * Bitmap解析器
 * User: ljx
 * Date: 2019/06/07
 * Time: 14:35
 */
public class BitmapParser implements Parser<Bitmap> {

    /**
     * @param response Http请求执行结果
     * @return Bitmap 对象
     * @throws IOException 网络异常等,RxJava的观察者会捕获此异常
     */
    @Override
    public Bitmap onParse(Response response) throws IOException {
        ResponseBody body = ExceptionHelper.throwIfFatal(response);
        LogUtil.log(response, false, null);
        return BitmapFactory.decodeStream(body.byteStream());
    }
}
