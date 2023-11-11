package rxhttp.wrapper.exception;


import java.io.IOException;

import okhttp3.Request;

/**
 * The purpose of this class is to print exceptions with extension information
 * User: ljx
 * Date: 2023/11/10
 * Time: 21:29
 */
public class ProxyException extends IOException {

    private final Throwable proxyCause;

    public ProxyException(Request request, Throwable throwable) {
        this(request.url().toString(), throwable);
    }

    public ProxyException(String message, Throwable throwable) {
        super(message);
        this.proxyCause = throwable;
        setStackTrace(throwable.getStackTrace());
    }

    @Override
    public String toString() {
        return proxyCause.toString() + ", " + getMessage();
    }
}
