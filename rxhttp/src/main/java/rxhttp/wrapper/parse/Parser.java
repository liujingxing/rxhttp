package rxhttp.wrapper.parse;

import java.io.IOException;

import okhttp3.Response;

/**
 *[okhttp3.Response] to T
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
public interface Parser<T> {

    T onParse(Response response) throws IOException;
}
