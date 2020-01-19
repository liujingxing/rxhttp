package rxhttp.wrapper.parse;


import java.io.IOException;

import okhttp3.Response;
import rxhttp.wrapper.exception.ExceptionHelper;
import rxhttp.wrapper.utils.LogUtil;

/**
 * 通过此解析器，可拿到{@link okhttp3.Response}对象
 * User: ljx
 * Date: 2020-01-19
 * Time: 10:14
 */
public class OkResponseParser implements Parser<Response> {

    @Override
    public Response onParse(Response response) throws IOException {
        ExceptionHelper.throwIfFatal(response);
        LogUtil.log(response, isOnResultDecoder(response), null);
        return response;
    }
}
