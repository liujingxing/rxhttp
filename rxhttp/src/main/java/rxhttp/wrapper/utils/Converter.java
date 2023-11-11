package rxhttp.wrapper.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Response;
import okhttp3.ResponseBody;
import rxhttp.Platform;
import rxhttp.wrapper.OkHttpCompat;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;

/**
 * User: ljx
 * Date: 2022/10/30
 * Time: 14:44
 */
public class Converter {

    public static <T> T convertTo(Response response, Type rawType, Type... types) throws IOException {
        return convert(response, ParameterizedTypeImpl.get(rawType, types));
    }

    public static <T> T convertToParameterized(Response response, Type rawType, Type... actualTypes) throws IOException {
        return convert(response, ParameterizedTypeImpl.getParameterized(rawType, actualTypes));
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(Response response, Type type) throws IOException {
        ResponseBody body = OkHttpCompat.throwIfFail(response);
        if (type == ResponseBody.class) {
            try {
                return (T) OkHttpCompat.buffer(body);
            } finally {
                body.close();
            }
        } else if (Platform.get().isAndroid() && type == Bitmap.class) {
            return (T) BitmapFactory.decodeStream(body.byteStream());
        } else {
            boolean needDecodeResult = OkHttpCompat.needDecodeResult(response);
            IConverter converter = OkHttpCompat.request(response).tag(IConverter.class);
            return converter.convert(body, type, needDecodeResult);
        }
    }
}
