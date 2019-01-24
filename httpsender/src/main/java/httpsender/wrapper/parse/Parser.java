package httpsender.wrapper.parse;

import java.io.IOException;

import io.reactivex.annotations.NonNull;
import okhttp3.Response;

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
public interface Parser<T> {

    T onParse(@NonNull Response response) throws IOException;

}
