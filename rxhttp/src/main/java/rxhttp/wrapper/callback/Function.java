package rxhttp.wrapper.callback;

import java.io.IOException;

public interface Function<T, R> {

    R apply(T t) throws IOException;
}
