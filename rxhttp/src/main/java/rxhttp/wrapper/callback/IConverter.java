package rxhttp.wrapper.callback;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * User: ljx
 * Date: 2019-11-19
 * Time: 22:54
 */
public interface IConverter {

    // ResponseBody convert to T
    @NotNull
    <T> T convert(@NotNull ResponseBody body, @NotNull Type type, boolean needDecodeResult) throws IOException;

    // T convert to RequestBody
    default <T> RequestBody convert(T value) throws IOException {
        return RequestBody.create(null, new byte[0]);
    }
}
