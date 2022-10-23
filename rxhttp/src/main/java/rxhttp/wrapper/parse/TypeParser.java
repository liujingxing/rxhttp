package rxhttp.wrapper.parse;

import java.lang.reflect.Type;

import rxhttp.wrapper.utils.TypeUtil;


/**
 * User: ljx
 * Date: 2022/10/19
 * Time: 20:17
 */
public abstract class TypeParser<T> implements Parser<T> {

    protected final Type[] types;

    public TypeParser() {
        types = TypeUtil.getActualTypeParameters(getClass());
    }

    protected TypeParser(Type... types) {
        this.types = types;
    }
}
