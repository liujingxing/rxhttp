package rxhttp.wrapper.exception;

import java.net.UnknownHostException;

/**
 * User: ljx
 * Date: 2018/2/5
 * Time: 16:59
 */
public class NetworkErrorException extends UnknownHostException {

    public NetworkErrorException() {
    }

    public NetworkErrorException(String message) {
        super(message);
    }
}
