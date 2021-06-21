package rxhttp.wrapper.utils;

import java.util.concurrent.TimeUnit;

/**
 * User: ljx
 * Date: 2019-11-30
 * Time: 18:37
 */
public class LogTime {

    private final long startNs;

    public LogTime() {
        this.startNs = System.nanoTime();
    }

    public long tookMs() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
    }
}
