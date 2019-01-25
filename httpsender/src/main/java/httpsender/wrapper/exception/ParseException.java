package httpsender.wrapper.exception;


import java.io.IOException;

import io.reactivex.annotations.Nullable;

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 22:29
 */
public class ParseException extends IOException {

    private String mErrorCode;

    public ParseException() {
    }

    public ParseException(String message) {
        this("", message);
    }

    public ParseException(String code, String message) {
        super(message);
        mErrorCode = code;
    }

    @Nullable
    @Override
    public String getLocalizedMessage() {
        return mErrorCode;
    }
}
