package rxhttp.wrapper.cookie;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * User: ljx
 * Date: 2019-12-29
 * Time: 20:47
 */
public interface ICookieJar extends CookieJar {

    @Override
    default void saveFromResponse(@NotNull HttpUrl url, @NotNull List<Cookie> cookies) {
        saveCookie(url, cookies);
    }

    @Override
    default List<Cookie> loadForRequest(@NotNull HttpUrl url) {
        return loadCookie(url);
    }

    /**
     * 保存url对应所有cookie
     * @param url  HttpUrl
     * @param cookies List
     */
    void saveCookie(HttpUrl url, List<Cookie> cookies);

    /**
     * 保存url对应所有cookie
     * @param url HttpUrl
     * @param cookie Cookie
     */
    void saveCookie(HttpUrl url, Cookie cookie);

    /**
     * 加载url所有的cookie
     * @param url HttpUrl
     * @return List
     */
    List<Cookie> loadCookie(HttpUrl url);

    /**
     * 移除url 对应的cookie
     * @param url HttpUrl
     */
    void removeCookie(HttpUrl url);

    /**
     * 移除所有cookie
     */
    void removeAllCookie();
}
