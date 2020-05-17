package rxhttp.wrapper.cahce;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;


/**
 * User: ljx
 * Date: 2020/5/15
 * Time: 23:14
 */
class HeadersVary {

    /**
     * Returns true if none of the Vary headers have changed between {@code cachedRequest} and {@code
     * newRequest}.
     */
    static boolean varyMatches(
        Response cachedResponse, Headers cachedRequest, Request newRequest) {
        for (String field : varyFields(cachedResponse)) {
            if (!equal(cachedRequest.values(field), newRequest.headers(field))) return false;
        }
        return true;
    }

    /**
     * Returns the subset of the headers in {@code response}'s request that impact the content of
     * response's body.
     */
    static Headers varyHeaders(Response response) {
        // Use the request headers sent over the network, since that's what the
        // response varies on. Otherwise OkHttp-supplied headers like
        // "Accept-Encoding: gzip" may be lost.
        Headers requestHeaders = response.networkResponse().request().headers();
        Headers responseHeaders = response.headers();
        return varyHeaders(requestHeaders, responseHeaders);
    }

    /**
     * Returns the subset of the headers in {@code requestHeaders} that impact the content of
     * response's body.
     */
    private static Headers varyHeaders(Headers requestHeaders, Headers responseHeaders) {
        Set<String> varyFields = varyFields(responseHeaders);
        if (varyFields.isEmpty()) return new Headers.Builder().build();

        Headers.Builder result = new Headers.Builder();
        for (int i = 0, size = requestHeaders.size(); i < size; i++) {
            String fieldName = requestHeaders.name(i);
            if (varyFields.contains(fieldName)) {
                result.add(fieldName, requestHeaders.value(i));
            }
        }
        return result.build();
    }

    private static Set<String> varyFields(Response response) {
        return varyFields(response.headers());
    }

    /**
     * Returns the names of the request headers that need to be checked for equality when caching.
     */
    private static Set<String> varyFields(Headers responseHeaders) {
        Set<String> result = Collections.emptySet();
        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
            if (!"Vary".equalsIgnoreCase(responseHeaders.name(i))) continue;

            String value = responseHeaders.value(i);
            if (result.isEmpty()) {
                result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            }
            for (String varyField : value.split(",")) {
                result.add(varyField.trim());
            }
        }
        return result;
    }

    /**
     * Returns true if two possibly-null objects are equal.
     */
    private static boolean equal(Object a, Object b) {
        return Objects.equals(a, b);
    }

}
