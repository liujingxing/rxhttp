package rxhttp.wrapper.callback;


import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rxhttp.wrapper.annotations.NonNull;

/**
 * User: ljx
 * Date: 2019-11-19
 * Time: 22:54
 */
public interface Converter {

    // ResponseBody convert to T
    @NonNull
    <T> T convert(@NonNull ResponseBody body, @NonNull Type type, boolean needDecodeResult) throws IOException;

    // T convert to RequestBody
    default <T> RequestBody convert(T value) throws IOException {
        return RequestBody.create(null, new byte[0]);
    }
}
