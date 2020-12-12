package rxhttp.wrapper.param;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Map.Entry;

import okhttp3.Headers;
import okhttp3.Headers.Builder;

/**
 * User: ljx
 * Date: 2019/1/22
 * Time: 13:58
 */
@SuppressWarnings("unchecked")
public interface IHeaders<P extends Param<P>> {

    Headers getHeaders();

    Headers.Builder getHeadersBuilder();

    P setHeadersBuilder(Headers.Builder builder);

    default String getHeader(String key) {
        return getHeadersBuilder().get(key);
    }

    default P addHeader(String key, String value) {
        getHeadersBuilder().add(key, value);
        return (P) this;
    }

    default P addNonAsciiHeader(String key, String value) {
        getHeadersBuilder().addUnsafeNonAscii(key, value);
        return (P) this;
    }

    default P setNonAsciiHeader(String key, String value) {
        Builder builder = getHeadersBuilder();
        builder.removeAll(key);
        builder.addUnsafeNonAscii(key, value);
        return (P) this;
    }

    default P addHeader(String line) {
        getHeadersBuilder().add(line);
        return (P) this;
    }

    default P addAllHeader(@NotNull Map<String, String> headers) {
        for (Entry<String, String> entry : headers.entrySet()) {
            addHeader(entry.getKey(), entry.getValue());
        }
        return (P) this;
    }

    default P addAllHeader(Headers headers) {
        getHeadersBuilder().addAll(headers);
        return (P) this;
    }

    default P setHeader(String key, String value) {
        getHeadersBuilder().set(key, value);
        return (P) this;
    }

    default P setAllHeader(@NotNull Map<String, String> headers) {
        for (Entry<String, String> entry : headers.entrySet()) {
            setHeader(entry.getKey(), entry.getValue());
        }
        return (P) this;
    }

    default P removeAllHeader(String key) {
        getHeadersBuilder().removeAll(key);
        return (P) this;
    }

    /**
     * 设置断点下载开始位置，结束位置默认为文件末尾
     *
     * @param startIndex 开始位置
     * @return Param
     */
    default P setRangeHeader(long startIndex) {
        return setRangeHeader(startIndex, -1);
    }

    /**
     * 设置断点下载范围
     * 注：
     * 1、开始位置小于0，及代表下载完整文件
     * 2、结束位置要大于开始位置，否则结束位置默认为文件末尾
     *
     * @param startIndex 开始位置
     * @param endIndex   结束位置
     * @return Param
     */
    default P setRangeHeader(long startIndex, long endIndex) {
        if (endIndex < startIndex) endIndex = -1;
        String headerValue = "bytes=" + startIndex + "-";
        if (endIndex >= 0) {
            headerValue = headerValue + endIndex;
        }
        return addHeader("RANGE", headerValue);
    }
}
