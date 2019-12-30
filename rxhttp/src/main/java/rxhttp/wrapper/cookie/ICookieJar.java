package rxhttp.wrapper.cookie;

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
    default void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        saveCookie(url, cookies);
    }

    @Override
    default List<Cookie> loadForRequest(HttpUrl url) {
        return loadCookie(url);
    }

    /**
     * 保存url对应所有cookie
     */
    void saveCookie(HttpUrl url, List<Cookie> cookies);

    /**
     * 保存url对应所有cookie
     */
    void saveCookie(HttpUrl url, Cookie cookie);

    /**
     * 加载url所有的cookie
     */
    List<Cookie> loadCookie(HttpUrl url);

    /**
     * 移除url 对应的cookie
     */
    void removeCookie(HttpUrl url);

    /**
     * 移除所有cookie
     */
    void removeAllCookie();
}
