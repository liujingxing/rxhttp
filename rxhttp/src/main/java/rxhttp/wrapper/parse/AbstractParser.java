package rxhttp.wrapper.parse;


import com.google.gson.internal.$Gson$Preconditions;
import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.Type;

import rxhttp.wrapper.utils.TypeUtil;

/**
 * User: ljx
 * Date: 2019/1/21
 * Time: 15:32
 */
public abstract class AbstractParser<T> implements Parser<T> {

    protected Type mType;

    public AbstractParser() {
        mType = TypeUtil.getActualTypeParameter(getClass(), 0);
    }

    public AbstractParser(Type type) {
        mType = $Gson$Types.canonicalize($Gson$Preconditions.checkNotNull(type));
    }
}
