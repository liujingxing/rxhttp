package httpsender.wrapper.exception;

/**
 * 空对象异常    Response.getCode()==100 && Response.getData()==null
 * User: ljx
 * Date: 2018/11/21
 * Time: 09:42
 */
public class EmptyObjectException extends ParseException {
    public EmptyObjectException() {
    }

    public EmptyObjectException(String message) {
        super(message);
    }

    public EmptyObjectException(String code, String message) {
        super(code, message);
    }

}
