package httpsender.wrapper.param;

import okhttp3.Headers;

/**
 * User: ljx
 * Date: 2019/1/22
 * Time: 13:58
 */
public interface HeadersBuilder {

    Headers getHeaders();

    String getHeader(String key);

    Headers.Builder getHeadersBuilder();

    Param setHeadersBuilder(Headers.Builder builder);

    Param addHeader(String key, String value);

    Param addHeader(String line);

    Param setHeader(String key, String value);

    Param removeAllHeader(String key);
}
