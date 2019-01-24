package httpsender.wrapper.parse;

import java.io.IOException;

import okhttp3.Response;

/**
 * 将Response对象解析成String对象
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
public class StringParser extends AbstractParser<String> {

    public static StringParser get() {
        return new StringParser();
    }

    @Override
    public String onParse(Response response) throws IOException {
        return getResult(response);
    }
}
