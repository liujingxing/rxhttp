package httpsender.wrapper.param;

import okhttp3.Request;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 17:24
 */
public interface RequestBuilder {

    Request buildRequest();
}
