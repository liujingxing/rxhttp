package httpsender.wrapper.exception;

/**
 * 失败异常    Response.getCode != 100
 * User: ljx
 * Date: 2018/11/21
 * Time: 10:12
 */
public class FailException extends ParseException {
    public FailException() {
    }

    public FailException(String message) {
        super(message);
    }

    public FailException(String code, String message) {
        super(code, message);
    }
}
