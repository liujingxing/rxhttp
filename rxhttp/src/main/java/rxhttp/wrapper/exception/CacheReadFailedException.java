package rxhttp.wrapper.exception;

import java.io.IOException;

import rxhttp.wrapper.cahce.CacheMode;

/**
 * 缓存读取失败异常，仅在 {@link CacheMode#ONLY_CACHE}模式下会抛出
 * User: ljx
 * Date: 2019-12-15
 * Time: 20:16
 */
public class CacheReadFailedException extends IOException {

    public CacheReadFailedException() {
    }

    public CacheReadFailedException(String message) {
        super(message);
    }

    public CacheReadFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheReadFailedException(Throwable cause) {
        super(cause);
    }
}
