package rxhttp.wrapper.parse;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;
import rxhttp.wrapper.OkHttpCompat;
import rxhttp.wrapper.entity.EmptyResponseBody;
import rxhttp.wrapper.entity.OkResponse;

/**
 * User: ljx
 * Date: 2022/9/22
 * Time: 21:53
 */
public class OkResponseParser<T> implements Parser<OkResponse<T>> {

    private final Parser<T> parser;

    public OkResponseParser(Parser<T> parser) {
        this.parser = parser;
    }

    @Override
    public OkResponse<T> onParse(@NotNull Response response) throws IOException {
        ResponseBody rawBody = response.body();

        // Remove the body's source (the only stateful object) so we can pass the response along.
        Response emptyResponse =
            response
                .newBuilder()
                .body(new EmptyResponseBody(rawBody.contentType(), rawBody.contentLength()))
                .build();


        if (!emptyResponse.isSuccessful()) {
            try {
                // Buffer the entire body to avoid future I/O.
                ResponseBody bufferedBody = OkHttpCompat.buffer(rawBody);
                return OkResponse.error(bufferedBody, emptyResponse);
            } finally {
                rawBody.close();
            }
        }
        int code = emptyResponse.code();
        if (code == 204 || code == 205) {
            rawBody.close();
            return OkResponse.success(null, emptyResponse);
        }

        T body = parser.onParse(response);
        return OkResponse.success(body, emptyResponse);
    }
}
